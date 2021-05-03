package eu.koboo.endpoint.core.protocols.natives;

import eu.koboo.endpoint.core.Endpoint;
import eu.koboo.nettyutils.BufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.util.ReferenceCountUtil;

import java.util.List;

public class NativeCodec extends ByteToMessageCodec<NativePacket> {

    private final Endpoint endpoint;

    public NativeCodec(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, NativePacket nativePacket, ByteBuf out) {
        try {
            ByteBuf payload = ctx.alloc().buffer();

            String classPath = nativePacket.getClassPath();

            if (classPath != null) {

                BufUtils.writeString(classPath, payload);
                nativePacket.write(payload);

                byte[] outArray = BufUtils.toArray(payload);

                // Write byte[] length to ByteBuf
                BufUtils.writeVarInt(outArray.length, out);

                // Write data-content to ByteBuf
                out.writeBytes(outArray);

            }
            ReferenceCountUtil.release(payload);
        } catch (Exception e) {
            endpoint.onException(getClass(), e);
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        try {
            // Check if contains VarInt-Length
            if (in.readableBytes() < 5) {
                return;
            }

            // Mark the reader-index
            in.markReaderIndex();

            // Decode VarIntLength
            int contentLength = BufUtils.readVarInt(in);

            // Is bytes exceeds length -> resetReaderIndex
            if (in.readableBytes() < contentLength) {
                in.resetReaderIndex();
                return;
            }

            ByteBuf outBuf = ctx.alloc().buffer(contentLength);
            in.readBytes(outBuf);

            String classPath = BufUtils.readString(outBuf);

            Class<? extends NativePacket> clazz = (Class<? extends NativePacket>) Class.forName(classPath);
            NativePacket nativePacket = clazz.newInstance();
            nativePacket.read(outBuf);

            out.add(nativePacket);

            ReferenceCountUtil.release(outBuf);
        } catch (Exception e) {
            endpoint.onException(getClass(), e);
        }
    }
}

