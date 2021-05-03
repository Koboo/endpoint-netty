package eu.koboo.endpoint.core.handler;

import eu.koboo.endpoint.core.Endpoint;
import eu.koboo.endpoint.core.events.channel.ChannelActionEvent;
import eu.koboo.endpoint.core.protocols.ReceiveEvent;
import eu.koboo.endpoint.core.protocols.natives.NativePacket;
import eu.koboo.endpoint.core.protocols.natives.NativeReceiveEvent;
import eu.koboo.endpoint.core.protocols.serializable.SerializablePacket;
import eu.koboo.endpoint.core.protocols.serializable.SerializableReceiveEvent;
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
            if(!endpoint.isClient() && channels != null)
                channels.add(ctx.channel());
            endpoint.eventHandler().callEvent(new ChannelActionEvent(ctx.channel(), ChannelActionEvent.Action.CONNECT));
        } catch (Exception e) {
            endpoint.onException(EndpointHandler.class, e);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        try {
            super.channelInactive(ctx);
            if(!endpoint.isClient() && channels != null)
                channels.remove(ctx.channel());
            endpoint.eventHandler().callEvent(new ChannelActionEvent(ctx.channel(), ChannelActionEvent.Action.DISCONNECT));
        } catch (Exception e) {
            endpoint.onException(EndpointHandler.class, e);
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            super.channelRead(ctx, msg);
            ReceiveEvent event = null;
            if (msg instanceof NativePacket)
                event = new NativeReceiveEvent(ctx.channel(), (NativePacket) msg);
            else if (msg instanceof SerializablePacket)
                event = new SerializableReceiveEvent(ctx.channel(), (SerializablePacket) msg);
            else
                endpoint.onException(getClass(), new RuntimeException("Error calling events of object!"));

            if (event != null) {
                ReceiveEvent receiveEvent = event;
                switch (endpoint.builder().getEventMode()) {
                    case SYNC:
                        endpoint.eventHandler().callEvent(receiveEvent);
                        break;
                    case SERVICE:
                        endpoint.executor().execute(() -> endpoint.eventHandler().callEvent(receiveEvent));
                        break;
                    case EVENT_LOOP:
                        ctx.executor().execute(() -> endpoint.eventHandler().callEvent(receiveEvent));
                        break;
                }
            }
        } catch (Exception e) {
            ReferenceCountUtil.release(msg);
            endpoint.onException(EndpointHandler.class, e);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        try {
            super.channelReadComplete(ctx);
            ctx.flush();
        } catch (Exception e) {
            endpoint.onException(EndpointHandler.class, e);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        endpoint.onException(EndpointHandler.class, cause);
    }
}
