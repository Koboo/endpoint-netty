package eu.koboo.endpoint.core.handler;

import eu.koboo.endpoint.core.Endpoint;
import eu.koboo.endpoint.core.events.channel.ChannelTimeoutEvent;
import eu.koboo.endpoint.core.events.channel.Timeout;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class EndpointIdleHandler extends ChannelDuplexHandler {

    private final Endpoint endpoint;

    public EndpointIdleHandler(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            if (idleStateEvent.state() == IdleState.WRITER_IDLE) {

                if (endpoint.builder().isUsingTimeouts())
                    endpoint.eventHandler().fireEvent(new ChannelTimeoutEvent(ctx.channel(), Timeout.WRITE));

                ctx.writeAndFlush(1);
            } else if (idleStateEvent.state() == IdleState.READER_IDLE) {

                if (endpoint.builder().isUsingTimeouts())
                    endpoint.eventHandler().fireEvent(new ChannelTimeoutEvent(ctx.channel(), Timeout.READ));
            }
        }
        super.userEventTriggered(ctx, evt);
    }
}

