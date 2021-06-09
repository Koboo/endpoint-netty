package eu.koboo.endpoint.core.codec.serial;

import eu.binflux.serial.core.SerializerPool;
import eu.binflux.serial.fst.FSTSerialization;
import eu.koboo.endpoint.core.Endpoint;
import eu.koboo.endpoint.core.codec.AbstractEndpointCodec;
import eu.koboo.endpoint.core.util.BufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

public class SerializableCodec extends AbstractEndpointCodec<SerializablePacket> {

    private final SerializerPool serializerPool;

    public SerializableCodec(Endpoint endpoint) {
        super(endpoint);
        serializerPool = new SerializerPool(FSTSerialization.class);
    }

    @Override
    public byte[] encodePacket(Channel channel, SerializablePacket packet) throws Exception {
        return serializerPool.serialize(packet);
    }

    @Override
    public SerializablePacket decodePacket(Channel channel, ByteBuf in) throws Exception {

        byte[] unwrappedBuffer = BufUtils.toArray(in);

        return serializerPool.deserialize(unwrappedBuffer);
    }

}
