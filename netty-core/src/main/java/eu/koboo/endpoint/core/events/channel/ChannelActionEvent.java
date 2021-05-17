package eu.koboo.endpoint.core.events.channel;

import eu.koboo.event.CallableEvent;
import io.netty.channel.Channel;

public class ChannelActionEvent implements CallableEvent {

    private final Channel ctx;
    private final Action action;

    public ChannelActionEvent(Channel ctx, Action action) {
        this.ctx = ctx;
        this.action = action;
    }

    public Channel getChannel() {
        return ctx;
    }

    public Action getAction() {
        return action;
    }

    public static enum Action {

        CONNECT,
        DISCONNECT;

    }
}
