package eu.koboo.endpoint.core.protocols.natives;

import eu.koboo.endpoint.core.protocols.ReceiveEvent;
import io.netty.channel.Channel;

public class NativeReceiveEvent extends ReceiveEvent<NativePacket> {

    public NativeReceiveEvent(Channel channel, NativePacket object) {
        super(channel, object);
    }
}
