package eu.koboo.endpoint.test.builder;

import eu.koboo.endpoint.core.builder.EndpointBuilder;
import eu.koboo.endpoint.core.util.Compression;

public class CompressTest extends AbstractBuilderTest {

    @Override
    public EndpointBuilder changeBuilder(EndpointBuilder builder) {
        return builder.logging(false)
                .compression(Compression.SNAPPY);
    }
}