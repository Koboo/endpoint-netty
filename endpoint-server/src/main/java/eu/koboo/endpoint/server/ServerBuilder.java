package eu.koboo.endpoint.server;

import eu.koboo.endpoint.core.builder.EndpointBuilder;

public class ServerBuilder {

    public static EndpointServer of(EndpointBuilder builder, int port) {
        return new EndpointServer(builder, port);
    }

    public static EndpointServer of(EndpointBuilder builder) {
        return new EndpointServer(builder, -1);
    }
}
