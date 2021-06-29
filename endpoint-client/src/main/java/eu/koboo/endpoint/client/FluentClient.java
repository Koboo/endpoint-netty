package eu.koboo.endpoint.client;

import eu.koboo.endpoint.core.builder.EndpointBuilder;
import eu.koboo.endpoint.core.builder.param.ErrorMode;
import eu.koboo.endpoint.core.codec.EndpointPacket;
import eu.koboo.endpoint.core.events.ReceiveEvent;
import eu.koboo.endpoint.core.events.channel.ChannelAction;
import eu.koboo.endpoint.core.events.channel.ChannelActionEvent;
import eu.koboo.endpoint.core.events.endpoint.EndpointAction;
import eu.koboo.endpoint.core.events.endpoint.EndpointActionEvent;
import eu.koboo.endpoint.core.events.message.ErrorEvent;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class FluentClient extends EndpointClient {

    public FluentClient(EndpointBuilder endpointBuilder, String host, int port) {
        super(endpointBuilder, host, port);
    }

    public FluentClient onStart(Runnable runnable) {
        registerEvent(EndpointActionEvent.class, event -> {
            if(event.getAction() == EndpointAction.START) {
                runnable.run();
            }
        });
        return this;
    }

    public FluentClient onStop(Runnable runnable) {
        registerEvent(EndpointActionEvent.class, event -> {
            if(event.getAction() == EndpointAction.STOP) {
                runnable.run();
            }
        });
        return this;
    }

    public FluentClient onConnect(Runnable runnable) {
        registerEvent(ChannelActionEvent.class, event -> {
            if(event.getAction() == ChannelAction.CONNECT) {
                runnable.run();
            }
        });
        return this;
    }

    public FluentClient onDisconnect(Runnable runnable) {
        registerEvent(ChannelActionEvent.class, event -> {
            if(event.getAction() == ChannelAction.DISCONNECT) {
                runnable.run();
            }
        });
        return this;
    }

    public FluentClient onError(BiConsumer<Class<?>, Throwable> error) {
        builder().errorMode(ErrorMode.EVENT);
        registerEvent(ErrorEvent.class, event -> error.accept(event.getClazz(), event.getThrowable()));
        return this;
    }

    public <P extends EndpointPacket> FluentClient onPacket(Class<P> packetClass, Consumer<P> biConsumer) {
        registerEvent(ReceiveEvent.class, event -> {
            if(event.getTypeObject().getClass().getName().equalsIgnoreCase(packetClass.getName())) {
                P packet = event.getTypeObject();
                biConsumer.accept(packet);
            }
        });
        return this;
    }

    public FluentClient changeAddress(String address, int port) {
        setAddress(address, port);
        return this;
    }

    public <P extends EndpointPacket> FluentClient send(P packet) {
        super.send(packet).syncUninterruptibly();
        return this;
    }

    public FluentClient connect() {
        return super.start() ? this : null;
    }
}
