package eu.koboo.endpoint.core.codec;

import io.netty.buffer.ByteBuf;

public interface EndpointPacket {

    void read(ByteBuf byteBuf);

    void write(ByteBuf byteBuf);

}

