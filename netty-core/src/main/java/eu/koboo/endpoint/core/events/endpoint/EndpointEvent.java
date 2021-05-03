package eu.koboo.endpoint.core.events.endpoint;

import eu.koboo.endpoint.core.Endpoint;
import eu.koboo.event.CallableEvent;

public class EndpointEvent implements CallableEvent {

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

    public static enum Action {

        INITIALIZE,
        START,
        STOP,
        CLOSE;

    }

}
