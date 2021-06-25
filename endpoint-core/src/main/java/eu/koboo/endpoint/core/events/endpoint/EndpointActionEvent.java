package eu.koboo.endpoint.core.events.endpoint;

import eu.koboo.endpoint.core.Endpoint;
import eu.koboo.endpoint.core.events.ConsumerEvent;

public class EndpointActionEvent implements ConsumerEvent {

    private final Endpoint endpoint;
    private final EndpointAction endpointAction;

    public EndpointActionEvent(Endpoint endpoint, EndpointAction endpointAction) {
        this.endpoint = endpoint;
        this.endpointAction = endpointAction;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public EndpointAction getAction() {
        return endpointAction;
    }

}
