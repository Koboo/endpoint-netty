package eu.koboo.endpoint.core.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

public interface EndpointCodec<T> {

    byte[] encodePacket(Channel channel, T packet) throws Exception;

    T decodePacket(Channel channel, ByteBuf in) throws Exception;
}
