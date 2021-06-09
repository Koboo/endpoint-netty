package eu.koboo.endpoint.core.codec.natives;

import io.netty.buffer.ByteBuf;

public interface NativePacket {

    void read(ByteBuf byteBuf);

    void write(ByteBuf byteBuf);

}

