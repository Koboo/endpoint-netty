package eu.koboo.endpoint.client;

import eu.koboo.endpoint.core.builder.EndpointBuilder;

public class ClientBuilder {

    public static EndpointClient of(EndpointBuilder builder) {
        return of(builder, null, -1);
    }

    public static EndpointClient of(EndpointBuilder builder, String address, int port) {
        return new EndpointClient(builder, address, port);
    }

    public static FluentClient fluentOf(EndpointBuilder builder) {
        return fluentOf(builder, null, -1);
    }

    public static FluentClient fluentOf(EndpointBuilder builder, String address, int port) {
        return new FluentClient(builder, address, port);
    }
}
