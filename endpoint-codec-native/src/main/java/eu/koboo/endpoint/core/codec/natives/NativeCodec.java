package eu.koboo.endpoint.core.codec.natives;

import eu.koboo.endpoint.core.Endpoint;
import eu.koboo.endpoint.core.codec.AbstractEndpointCodec;
import eu.koboo.endpoint.core.util.BufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

public class NativeCodec extends AbstractEndpointCodec<NativePacket> {

    public NativeCodec(Endpoint endpoint) {
        super(endpoint);
    }

    @Override
    public byte[] encodePacket(Channel channel, NativePacket packet) throws Exception {

        ByteBuf payload = channel.alloc().buffer();

        int oid = endpoint.builder().getIdByClass(packet.getClass());

        if(oid != -1) {

            BufUtils.writeVarInt(oid, payload);
            packet.write(payload);

            return BufUtils.toArray(payload);
        }
        return null;
    }

    @Override
    public NativePacket decodePacket(Channel channel, ByteBuf in) throws Exception {

        int oid = BufUtils.readVarInt(in);

        Class<?> clazz = endpoint.builder().getClassById(oid);
        if(clazz != null) {
            NativePacket nativePacket = (NativePacket) clazz.newInstance();
            nativePacket.read(in);

            return nativePacket;
        }
        return null;
    }

}

