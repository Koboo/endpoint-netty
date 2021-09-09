package eu.koboo.endpoint.server;

import eu.koboo.endpoint.core.builder.EndpointBuilder;
import eu.koboo.endpoint.core.builder.param.ErrorMode;
import eu.koboo.endpoint.core.codec.EndpointPacket;
import eu.koboo.endpoint.core.events.ReceiveEvent;
import eu.koboo.endpoint.core.events.channel.ChannelAction;
import eu.koboo.endpoint.core.events.channel.ChannelActionEvent;
import eu.koboo.endpoint.core.events.endpoint.EndpointAction;
import eu.koboo.endpoint.core.events.endpoint.EndpointActionEvent;
import eu.koboo.endpoint.core.events.message.ErrorEvent;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class FluentServer extends EndpointServer {

    public FluentServer(EndpointBuilder endpointBuilder, int port) {
        super(endpointBuilder, port);
    }

    public FluentServer onStart(Runnable runnable) {
        registerEvent(EndpointActionEvent.class, event -> {
            if (event.getAction() == EndpointAction.START) {
                runnable.run();
            }
        });
        return this;
    }

    public FluentServer onStop(Runnable runnable) {
        registerEvent(EndpointActionEvent.class, event -> {
            if (event.getAction() == EndpointAction.STOP) {
                runnable.run();
            }
        });
        return this;
    }

    public FluentServer onConnect(Consumer<Channel> consumer) {
        registerEvent(ChannelActionEvent.class, event -> {
            if (event.getAction() == ChannelAction.CONNECT) {
                consumer.accept(event.getChannel());
            }
        });
        return this;
    }

    public FluentServer onDisconnect(Consumer<Channel> consumer) {
        registerEvent(ChannelActionEvent.class, event -> {
            if (event.getAction() == ChannelAction.DISCONNECT) {
                consumer.accept(event.getChannel());
            }
        });
        return this;
    }

    public FluentServer onError(BiConsumer<Class<?>, Throwable> error) {
        builder().errorMode(ErrorMode.EVENT);
        registerEvent(ErrorEvent.class, event -> error.accept(event.getClazz(), event.getThrowable()));
        return this;
    }

    public <P extends EndpointPacket> FluentServer onPacket(Class<P> packetClass, BiConsumer<Channel, P> biConsumer) {
        registerEvent(ReceiveEvent.class, event -> {
            if (event.getTypeObject().getClass().getName().equalsIgnoreCase(packetClass.getName())) {
                P packet = event.getTypeObject();
                biConsumer.accept(event.getChannel(), packet);
            }
        });
        return this;
    }

    public FluentServer sendPacket(Channel channel, EndpointPacket packet) {
        ChannelFuture future = super.send(channel, packet);
        if(future != null) {
            future.syncUninterruptibly();
        }
        return this;
    }

    public FluentServer broadcastPacket(EndpointPacket packet) {
        super.broadcast(packet);
        return this;
    }

    public FluentServer changePort(int port) {
        super.setPort(port);
        return this;
    }

    public FluentServer bind() {
        return super.start() ? this : null;
    }
}
