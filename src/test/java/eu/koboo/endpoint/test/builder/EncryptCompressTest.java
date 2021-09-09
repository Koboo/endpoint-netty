package eu.koboo.endpoint.test.builder;

import eu.koboo.endpoint.core.builder.EndpointBuilder;
import eu.koboo.endpoint.core.util.Compression;
import eu.koboo.endpoint.test.common.AbstractBuilderTest;

public class EncryptCompressTest extends AbstractBuilderTest {

  @Override
  public EndpointBuilder changeBuilder(EndpointBuilder builder) {
    return builder.logging(true)
        .password("ThisIsMyPassword")
        .compression(Compression.SNAPPY);
  }
}
