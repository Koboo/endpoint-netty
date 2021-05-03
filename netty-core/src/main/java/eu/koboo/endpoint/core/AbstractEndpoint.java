
package eu.koboo.endpoint.core;

import eu.koboo.endpoint.core.builder.EndpointBuilder;
import eu.koboo.endpoint.core.builder.param.Protocol;
import eu.koboo.endpoint.core.events.endpoint.*;
import eu.koboo.endpoint.core.events.message.ErrorEvent;
import eu.koboo.event.EventBus;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class AbstractEndpoint implements Endpoint {

    private final EndpointBuilder endpointBuilder;
    private final EventBus eventBus;
    private final ExecutorService executor;

    public AbstractEndpoint(EndpointBuilder endpointBuilder) {
        this.endpointBuilder = endpointBuilder;
        this.eventBus = new EventBus();
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    @Override
    public boolean start() {
        eventBus.callEvent(new EndpointEvent(this, EndpointEvent.Action.START));
        return true;
    }

    @Override
    public boolean stop() {
        executor.shutdownNow();
        eventBus.callEvent(new EndpointEvent(this, EndpointEvent.Action.STOP));
        return true;
    }

    @Override
    public boolean close() {
        eventBus.callEvent(new EndpointEvent(this, EndpointEvent.Action.CLOSE));
        return true;
    }

    @Override
    public EndpointBuilder builder() {
        return endpointBuilder;
    }

    @Override
    public EventBus eventHandler() {
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
                eventBus.callEvent(new ErrorEvent(clazz, error));
                break;
            case STACK_TRACE:
                error.printStackTrace();
                break;
        }
    }
}
