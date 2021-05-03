package eu.koboo.endpoint.core.protocols;

import eu.koboo.event.CallableEvent;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public class ReceiveEvent<T> implements CallableEvent {

    private final Channel ctx;
    private final T object;

    public ReceiveEvent(Channel ctx, T object) {
        this.ctx = ctx;
        this.object = object;
    }

    public Channel getChannel() {
        return ctx;
    }

    public T getTypeObject() {
        return object;
    }
}
