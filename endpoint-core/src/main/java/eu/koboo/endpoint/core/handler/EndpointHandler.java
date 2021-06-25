package eu.koboo.endpoint.core.handler;

import eu.koboo.endpoint.core.Endpoint;
import eu.koboo.endpoint.core.events.ReceiveEvent;
import eu.koboo.endpoint.core.events.channel.ChannelAction;
import eu.koboo.endpoint.core.events.channel.ChannelActionEvent;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.util.ReferenceCountUtil;

public class EndpointHandler extends ChannelInboundHandlerAdapter {

    private final Endpoint endpoint;
    private final ChannelGroup channels;

    public EndpointHandler(Endpoint endpoint, ChannelGroup channels) {
        this.endpoint = endpoint;
        this.channels = channels;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        try {
            super.channelActive(ctx);
            if (!endpoint.isClient() && channels != null)
                channels.add(ctx.channel());
            endpoint.eventHandler().fireEvent(new ChannelActionEvent(ctx.channel(), ChannelAction.CONNECT));
        } catch (Exception e) {
            endpoint.onException(EndpointHandler.class, e);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        try {
            super.channelInactive(ctx);
            if (!endpoint.isClient() && channels != null)
                channels.remove(ctx.channel());
            endpoint.eventHandler().fireEvent(new ChannelActionEvent(ctx.channel(), ChannelAction.DISCONNECT));
        } catch (Exception e) {
            endpoint.onException(EndpointHandler.class, e);
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            super.channelRead(ctx, msg);

            ReceiveEvent event = new ReceiveEvent(ctx.channel(), msg);
            endpoint.eventHandler().fireEvent(event);
        } catch (Exception e) {
            ReferenceCountUtil.release(msg);
            endpoint.onException(EndpointHandler.class, e);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        try {
            ctx.flush();
            super.channelReadComplete(ctx);
        } catch (Exception e) {
            endpoint.onException(EndpointHandler.class, e);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        endpoint.onException(EndpointHandler.class, cause);
    }
}
