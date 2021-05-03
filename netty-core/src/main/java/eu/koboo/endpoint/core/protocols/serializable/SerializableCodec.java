package eu.koboo.endpoint.core.protocols.serializable;

import eu.koboo.endpoint.core.Endpoint;
import eu.koboo.nettyutils.BufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

public class SerializableCodec extends ByteToMessageCodec<SerializablePacket> {

    private final Endpoint endpoint;

    public SerializableCodec(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, SerializablePacket msg, ByteBuf out) {

        try {
            // Encode object to byte[]
            byte[] outArray = endpoint.builder().getSerializerPool().serialize(msg);

            // Write byte[] length to ByteBuf
            BufUtils.writeVarInt(outArray.length, out);

            // Write data-content to ByteBuf
            out.writeBytes(outArray);
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

            // Read content as byte[]
            byte[] packetContent = new byte[contentLength];
            in.readBytes(packetContent);

            // Read content as byte[]
            Object object = endpoint.builder().getSerializerPool().deserialize(packetContent);

            // Add object to output-list
            out.add(object);
        } catch (Exception e) {
            endpoint.onException(getClass(), e);
        }
    }

}
