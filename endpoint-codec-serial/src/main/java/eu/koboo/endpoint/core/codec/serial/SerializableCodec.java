package eu.koboo.endpoint.core.codec.serial;

import eu.koboo.endpoint.core.Endpoint;
import eu.koboo.endpoint.core.codec.AbstractEndpointCodec;
import eu.koboo.endpoint.core.util.BufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

public class SerializableCodec extends AbstractEndpointCodec<SerializablePacket> {

    public SerializableCodec(Endpoint endpoint) {
        super(endpoint);
    }

    @Override
    public byte[] encodePacket(Channel channel, SerializablePacket packet) throws Exception {
        return endpoint.builder().getSerializerPool().serialize(packet);
    }

    @Override
    public SerializablePacket decodePacket(Channel channel, ByteBuf in) throws Exception {

        byte[] unwrappedBuffer = BufUtils.toArray(in);

        SerializablePacket packet = endpoint.builder().getSerializerPool().deserialize(unwrappedBuffer);

        return packet;
    }

}
