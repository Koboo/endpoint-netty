package eu.koboo.endpoint.core;

import eu.koboo.endpoint.core.builder.EndpointBuilder;
import eu.koboo.endpoint.core.events.EventHandler;
import eu.koboo.endpoint.core.events.endpoint.EndpointAction;
import eu.koboo.endpoint.core.events.endpoint.EndpointActionEvent;
import eu.koboo.endpoint.core.events.message.ErrorEvent;
import io.netty.channel.Channel;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class AbstractEndpoint implements Endpoint {

    protected final EndpointBuilder endpointBuilder;
    private final EventHandler eventBus;
    private final ExecutorService executor;
    protected final EventExecutorGroup executorGroup;
    protected final List<EventLoopGroup> eventLoopGroupList;
    protected Channel channel;

    public AbstractEndpoint(EndpointBuilder endpointBuilder) {
        this.endpointBuilder = endpointBuilder;
        this.eventBus = new EventHandler();
        int cores = Runtime.getRuntime().availableProcessors();
        this.executor = Executors.newFixedThreadPool(cores * 2);
        if (builder().isProcessing()) {
            this.executorGroup = new DefaultEventExecutorGroup(cores * 2);
        } else {
            this.executorGroup = null;
        }
        this.eventLoopGroupList = new ArrayList<>();
    }

    @Override
    public boolean start() {
        eventBus.fireEvent(new EndpointActionEvent(this, EndpointAction.START));
        return true;
    }

    @Override
    public boolean stop() {
        try {
            boolean close = this.close();
            executor.shutdownNow();
            for (EventLoopGroup eventLoopGroup : eventLoopGroupList) {
                eventLoopGroup.shutdownGracefully();
            }
            if (executorGroup != null)
                executorGroup.shutdownGracefully();
            eventBus.fireEvent(new EndpointActionEvent(this, EndpointAction.STOP));
            return close;
        } catch (Exception e) {
            onException(getClass(), e);
        }
        return false;
    }

    @Override
    public boolean close() {
        try {
            if (channel != null && channel.isOpen() && channel.isActive())
                channel.close().sync();
            eventBus.fireEvent(new EndpointActionEvent(this, EndpointAction.CLOSE));
            return true;
        } catch (InterruptedException e) {
            onException(getClass(), e);
        }
        return false;
    }

    @Override
    public EndpointBuilder builder() {
        return endpointBuilder;
    }

    @Override
    public EventHandler eventHandler() {
        return eventBus;
    }


    @Override
    public boolean isConnected() {
        return channel != null && channel.isOpen() && channel.isActive();
    }

    @Override
    public void onException(Class clazz, Throwable error) {
        switch (endpointBuilder.getErrorMode()) {
            case EVENT:
                eventBus.fireEvent(new ErrorEvent(clazz, error));
                break;
            case STACK_TRACE:
                error.printStackTrace();
                break;
        }
    }
}
