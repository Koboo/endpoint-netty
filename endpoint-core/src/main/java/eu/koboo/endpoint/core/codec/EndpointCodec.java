package eu.koboo.endpoint.core.codec;

import eu.koboo.endpoint.core.Endpoint;
import eu.koboo.endpoint.core.util.BufUtils;
import eu.koboo.endpoint.core.util.EncryptUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import javax.crypto.SecretKey;
import java.util.List;
import java.util.function.Supplier;

public class EndpointCodec extends ByteToMessageCodec<EndpointPacket> {

    protected final Endpoint endpoint;
    private final SecretKey secretKey;

    public EndpointCodec(Endpoint endpoint) {
        this.endpoint = endpoint;
        if (endpoint.builder().getEncryption() != null)
            secretKey = EncryptUtils.getKeyFromPassword(endpoint.builder().getEncryption());
        else
            secretKey = null;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, EndpointPacket endpointPacket, ByteBuf out) {
        try {
            byte[] encoded = encodePacket(ctx.channel(), endpointPacket);

            if (encoded.length > 1) {

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

            EndpointPacket packet = decodePacket(ctx.channel(), inBuffer);

            if (packet != null) {
                out.add(packet);
            }

            if(inBuffer.refCnt() > 1)
                inBuffer.release();
        } catch (Exception e) {
            endpoint.onException(getClass(), e);
        }
    }

    public byte[] encodePacket(Channel channel, EndpointPacket packet) throws Exception {

        ByteBuf payload = channel.alloc().buffer();

        int oid = endpoint.builder().getId(packet);

        if (oid != -1) {

            BufUtils.writeVarInt(oid, payload);
            packet.write(payload);

            byte[] payloadBuffer = BufUtils.toArray(payload);
            payload.release();

            if (secretKey != null) {
                payloadBuffer = EncryptUtils.encrypt(payloadBuffer, secretKey);
            }

            return payloadBuffer;
        }
        return null;
    }

    public EndpointPacket decodePacket(Channel channel, ByteBuf in) throws Exception {

        if(secretKey != null) {
            byte[] encrypted = new byte[in.readableBytes()];
            in.readBytes(encrypted);
            in.release();
            byte[] decrypted = EncryptUtils.decrypt(encrypted, secretKey);
            in = channel.alloc().buffer();
            in.writeBytes(decrypted);
        }

        int oid = BufUtils.readVarInt(in);

        Supplier<? extends EndpointPacket> supplier = endpoint.builder().getSupplier(oid);
        if (supplier != null) {
            EndpointPacket endpointPacket = supplier.get();
            endpointPacket.read(in);

            in.release();

            return endpointPacket;
        } else {
            throw new NullPointerException("No supplier found of id '" + oid + "'");
        }
    }

}

