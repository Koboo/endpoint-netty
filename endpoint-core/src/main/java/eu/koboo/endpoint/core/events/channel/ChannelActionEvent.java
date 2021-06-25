package eu.koboo.endpoint.core.events.channel;

import eu.koboo.endpoint.core.events.ConsumerEvent;
import io.netty.channel.Channel;

public class ChannelActionEvent implements ConsumerEvent {

    private final Channel ctx;
    private final ChannelAction channelAction;

    public ChannelActionEvent(Channel ctx, ChannelAction channelAction) {
        this.ctx = ctx;
        this.channelAction = channelAction;
    }

    public Channel getChannel() {
        return ctx;
    }

    public ChannelAction getAction() {
        return channelAction;
    }

}
