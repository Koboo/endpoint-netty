package eu.koboo.endpoint.core;

import eu.koboo.endpoint.core.builder.EndpointBuilder;
import eu.koboo.endpoint.core.events.EventHandler;
import eu.koboo.endpoint.core.events.endpoint.EndpointAction;
import eu.koboo.endpoint.core.events.endpoint.EndpointActionEvent;
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
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    }

    @Override
    public boolean start() {
        eventBus.fireEvent(new EndpointActionEvent(this, EndpointAction.START));
        return true;
    }

    @Override
    public boolean stop() {
        executor.shutdownNow();
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
