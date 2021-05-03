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

    public Channel getCtx() {
        return ctx;
    }

    public Action getType() {
        return action;
    }

    public static enum Action {

        CONNECT,
        DISCONNECT;

    }
}
