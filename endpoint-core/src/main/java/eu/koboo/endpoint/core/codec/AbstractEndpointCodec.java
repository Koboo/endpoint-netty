package eu.koboo.endpoint.core.codec;

import eu.koboo.endpoint.core.Endpoint;
import eu.koboo.endpoint.core.util.BufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

public abstract class AbstractEndpointCodec<T> extends ByteToMessageCodec<T> implements EndpointCodec<T> {

    protected final Endpoint endpoint;

    public AbstractEndpointCodec(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    public abstract byte[] encodePacket(Channel channel, T packet) throws Exception;

    public abstract T decodePacket(Channel channel, ByteBuf in) throws Exception;

    @Override
    protected void encode(ChannelHandlerContext ctx, T nativePacket, ByteBuf out) {
        try {
            byte[] encoded = encodePacket(ctx.channel(), nativePacket);

            if(encoded.length > 1) {

                BufUtils.writeVarInt(encoded.length, out);
                out.writeBytes(encoded);

            }
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

            ByteBuf inBuffer = ctx.alloc().buffer(contentLength);
            in.readBytes(inBuffer);

            T packet = decodePacket(ctx.channel(), inBuffer);

            if(packet != null) {
                out.add(packet);
            }

            inBuffer.release();
        } catch (Exception e) {
            endpoint.onException(getClass(), e);
        }
    }
}

