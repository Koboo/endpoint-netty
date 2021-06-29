package eu.koboo.endpoint.test.builder;

import eu.koboo.endpoint.core.builder.EndpointBuilder;

public class NoProcessTest extends AbstractBuilderTest {

    @Override
    public EndpointBuilder changeBuilder(EndpointBuilder builder) {
        return builder.logging(false)
                .asyncProcessing(false);
    }
}