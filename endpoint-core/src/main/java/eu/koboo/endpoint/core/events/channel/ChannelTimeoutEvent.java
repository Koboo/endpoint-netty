package eu.koboo.endpoint.core.events.channel;

import eu.koboo.endpoint.core.events.ConsumerEvent;
import io.netty.channel.Channel;

public class ChannelTimeoutEvent implements ConsumerEvent {

    private final Channel channel;
    private final Timeout timeout;

    public ChannelTimeoutEvent(Channel channel, Timeout timeout) {
        this.channel = channel;
        this.timeout = timeout;
    }

    public Channel getChannel() {
        return channel;
    }

    public Timeout getType() {
        return timeout;
    }
}
