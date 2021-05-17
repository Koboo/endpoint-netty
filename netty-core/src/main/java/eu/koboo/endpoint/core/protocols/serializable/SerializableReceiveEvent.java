package eu.koboo.endpoint.core.protocols.serializable;

import eu.koboo.endpoint.core.protocols.ReceiveEvent;
import io.netty.channel.Channel;

public class SerializableReceiveEvent extends ReceiveEvent<SerializablePacket> {

    public SerializableReceiveEvent(Channel channel, SerializablePacket object) {
        super(channel, object);
    }

}
