package eu.koboo.endpoint.core.events.endpoint;

import eu.koboo.endpoint.core.Endpoint;
import eu.koboo.endpoint.core.events.EventHandler;

public class EndpointEvent implements EventHandler.ConsumerEvent {

    private final Endpoint endpoint;
    private final Action action;

    public EndpointEvent(Endpoint endpoint, Action action) {
        this.endpoint = endpoint;
        this.action = action;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public Action getAction() {
        return action;
    }

    public enum Action {

        START, RECONNECT, STOP, CLOSE
    }

}
