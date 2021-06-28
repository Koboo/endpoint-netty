package eu.koboo.endpoint.test.builder;

import eu.koboo.endpoint.core.builder.EndpointBuilder;
import eu.koboo.endpoint.core.codec.EndpointPacket;
import eu.koboo.endpoint.test.TestRequest;

import java.util.function.Supplier;

public class DefaultTest extends AbstractBuilderTest {

    @Override
    public EndpointBuilder changeBuilder(EndpointBuilder builder) {
        return builder.logging(false)
                .registerPacket(1, new Supplier<TestRequest>() {
                    @Override
                    public TestRequest get() {
                        return new TestRequest();
                    }
                });
    }
}
