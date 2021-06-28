package eu.koboo.endpoint.core;

import eu.koboo.endpoint.core.builder.EndpointBuilder;
import eu.koboo.endpoint.core.events.EventHandler;
import eu.koboo.endpoint.core.events.endpoint.EndpointAction;
import eu.koboo.endpoint.core.events.endpoint.EndpointActionEvent;
import eu.koboo.endpoint.core.events.message.ErrorEvent;
import io.netty.channel.Channel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class AbstractEndpoint implements Endpoint {

    protected final EndpointBuilder endpointBuilder;
    private final EventHandler eventBus;
    private final ExecutorService executor;
    protected final EventExecutorGroup executorGroup;
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
    }

    @Override
    public boolean start() {
        eventBus.fireEvent(new EndpointActionEvent(this, EndpointAction.START));
        return true;
    }

    @Override
    public boolean stop() {
        executor.shutdownNow();
        if(executorGroup != null)
            executorGroup.shutdownGracefully();
        eventBus.fireEvent(new EndpointActionEvent(this, EndpointAction.STOP));
        return true;
    }

    @Override
    public boolean close() {
        eventBus.fireEvent(new EndpointActionEvent(this, EndpointAction.CLOSE));
        return true;
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
