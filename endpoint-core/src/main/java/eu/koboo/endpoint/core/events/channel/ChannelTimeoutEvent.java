package eu.koboo.endpoint.core.events.channel;

import eu.koboo.endpoint.core.events.EventHandler;
import io.netty.channel.Channel;

public class ChannelTimeoutEvent implements EventHandler.ConsumerEvent {

    private final Channel channel;
    private final Type type;

    public ChannelTimeoutEvent(Channel channel, Type type) {
        this.channel = channel;
        this.type = type;
    }

    public Channel getChannel() {
        return channel;
    }

    public Type getType() {
        return type;
    }

    public enum Type {

        READ, WRITE
    }
}
