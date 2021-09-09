package eu.koboo.endpoint.test.common;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import eu.koboo.endpoint.client.ClientBuilder;
import eu.koboo.endpoint.client.EndpointClient;
import eu.koboo.endpoint.core.builder.EndpointBuilder;
import eu.koboo.endpoint.core.events.ReceiveEvent;
import eu.koboo.endpoint.core.events.message.LogEvent;
import eu.koboo.endpoint.server.EndpointServer;
import eu.koboo.endpoint.server.ServerBuilder;
import eu.koboo.endpoint.test.TestConstants;
import eu.koboo.endpoint.test.TestRequest;
import io.netty.channel.ChannelFuture;
import java.util.function.Consumer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public abstract class AbstractBuilderTest {

  public static EndpointServer server;
  public static EndpointClient client;

  @BeforeClass
  public static void setupClass() {
  }

  @AfterClass
  public static void afterClass() {
  }

  @Test
  public void test() throws Exception {
    System.out.println("== Init-Sequence == ");
    System.out.println("== '" + testName() + "' == ");

    EndpointBuilder builder = changeBuilder(TestConstants.BUILDER);

    Consumer<LogEvent> logEventConsumer = event -> System.out.println(event.getMessage());

    server = ServerBuilder.of(builder, 54321);
    server.registerEvent(LogEvent.class, logEventConsumer);
    server.registerEvent(ReceiveEvent.class, event -> {
      if (event.getTypeObject() instanceof TestRequest) {
        TestRequest request = event.getTypeObject();

        assertEquals(request.getTestString(), TestConstants.testString);
        assertEquals(request.getTestLong(), TestConstants.testLong);
        assertArrayEquals(request.getTestBytes(), TestConstants.testBytes);

        System.out.println("== Received and Asserted TestRequest == ");
      }
    });

    client = ClientBuilder.of(builder, "localhost", 54321);
    client.registerEvent(LogEvent.class, logEventConsumer);

    System.out.println("== Start-Sequence == ");

    System.out.println("== Server-Start == ");
    assertTrue(server.start());
    Thread.sleep(100L);
    System.out.println("== Client-Start == ");
    assertTrue(client.start());

    System.out.println("== Wait for connectivity == ");
    Thread.sleep(100L);

    System.out.println("== Send-Sequence == ");
    ChannelFuture future = client.send(TestConstants.TEST_REQUEST);
    if (future != null) {
      future.sync();
      System.out.println("== Sync-Request == ");
    }

    System.out.println("== Wait for delivery == ");
    Thread.sleep(100L);

    System.out.println("== Stop-Sequence == ");
    System.out.println("== Client-Stop == ");
    assertTrue(client.stop());
    Thread.sleep(100L);
    System.out.println("== Server-Stop == ");
    assertTrue(server.stop());
  }

  public String testName() {
    return getClass().getSimpleName();
  }

  public abstract EndpointBuilder changeBuilder(EndpointBuilder builder);

}
