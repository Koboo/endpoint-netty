package eu.koboo.endpoint.core.protocols.natives;

import io.netty.buffer.ByteBuf;

public interface NativePacket {

    default String getClassPath() {
        return getClass().getName();
    }

    void read(ByteBuf byteBuf);

    void write(ByteBuf byteBuf);

}

