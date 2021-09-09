package eu.koboo.endpoint.test.common;

import eu.koboo.endpoint.core.builder.EndpointBuilder;

public class DefaultTest extends AbstractBuilderTest {

    @Override
    public EndpointBuilder changeBuilder(EndpointBuilder builder) {
        return builder;
    }
}
