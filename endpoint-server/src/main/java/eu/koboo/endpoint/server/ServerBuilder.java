package eu.koboo.endpoint.server;

import eu.koboo.endpoint.core.builder.EndpointBuilder;
import eu.koboo.endpoint.core.util.DefaultSettings;

public class ServerBuilder {

    public static EndpointServer of(EndpointBuilder builder, int port) {
        return new EndpointServer(builder, port);
    }

    public static EndpointServer of(EndpointBuilder builder) {
        return new EndpointServer(builder, -1);
    }

    public static EndpointServer defaultOf(EndpointBuilder builder) {
        return of(builder, DefaultSettings.DEFAULT_PORT);
    }

    public static FluentServer fluentOf(EndpointBuilder builder) {
        return fluentOf(builder, -1);
    }

    public static FluentServer fluentOf(EndpointBuilder builder, int port) {
        return new FluentServer(builder, port);
    }

    public static FluentServer defaultFluentOf(EndpointBuilder builder) {
        return fluentOf(builder, DefaultSettings.DEFAULT_PORT);
    }
}
