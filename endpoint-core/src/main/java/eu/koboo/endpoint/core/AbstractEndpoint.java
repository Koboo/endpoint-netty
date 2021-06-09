package eu.koboo.endpoint.core;

import eu.koboo.endpoint.core.builder.EndpointBuilder;
import eu.koboo.endpoint.core.events.EventHandler;
import eu.koboo.endpoint.core.events.endpoint.EndpointEvent;
import eu.koboo.endpoint.core.events.message.ErrorEvent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class AbstractEndpoint implements Endpoint {

    protected final EndpointBuilder endpointBuilder;
    private final EventHandler eventBus;
    private final ExecutorService executor;

    public AbstractEndpoint(EndpointBuilder endpointBuilder) {
        this.endpointBuilder = endpointBuilder;
        this.eventBus = new EventHandler();
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    @Override
    public boolean start() {
        eventBus.handleEvent(new EndpointEvent(this, EndpointEvent.Action.START));
        return true;
    }

    @Override
    public boolean stop() {
        executor.shutdownNow();
        eventBus.handleEvent(new EndpointEvent(this, EndpointEvent.Action.STOP));
        return true;
    }

    @Override
    public boolean close() {
        eventBus.handleEvent(new EndpointEvent(this, EndpointEvent.Action.CLOSE));
        return true;
    }

    @Override
    public EndpointBuilder builder() {
        return endpointBuilder;
    }

    @Override
    public EventHandler eventHandler() {
        return this.eventBus;
    }

    @Override
    public ExecutorService executor() {
        return this.executor;
    }

    @Override
    public void onException(Class clazz, Throwable error) {
        switch (endpointBuilder.getErrorMode()) {
            case EVENT:
                eventBus.handleEvent(new ErrorEvent(clazz, error));
                break;
            case STACK_TRACE:
                error.printStackTrace();
                break;
        }
    }
}
