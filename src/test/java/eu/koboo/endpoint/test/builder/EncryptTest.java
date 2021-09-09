package eu.koboo.endpoint.test.builder;

import eu.koboo.endpoint.core.builder.EndpointBuilder;
import eu.koboo.endpoint.test.common.AbstractBuilderTest;

public class EncryptTest extends AbstractBuilderTest {

    @Override
    public EndpointBuilder changeBuilder(EndpointBuilder builder) {
        return builder.logging(false)
                .password("ThisIsMyStrongPassword");
    }
}
