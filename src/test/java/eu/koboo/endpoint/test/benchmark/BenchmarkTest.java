package eu.koboo.endpoint.test.benchmark;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import eu.koboo.endpoint.client.ClientBuilder;
import eu.koboo.endpoint.client.EndpointClient;
import eu.koboo.endpoint.core.events.ReceiveEvent;
import eu.koboo.endpoint.server.EndpointServer;
import eu.koboo.endpoint.server.ServerBuilder;
import eu.koboo.endpoint.test.TestConstants;
import eu.koboo.endpoint.test.TestRequest;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class BenchmarkTest {

  private static final int amount = 5_000;
  public static EndpointServer server;
  public static EndpointClient client;
  public static AtomicInteger counter;
  public static AtomicInteger average;

  @BeforeClass
  public static void setupClass() {
    System.out.println("== Test RandomRequest/Sec Behaviour == ");

    server = ServerBuilder.of(TestConstants.BUILDER, 54321);
    server.registerEvent(ReceiveEvent.class, event -> {
      if (event.getTypeObject() instanceof TestRequest) {
        TestRequest request = event.getTypeObject();
        assertEquals(request.getTestString(), TestConstants.testString);
        assertEquals(request.getTestLong(), TestConstants.testLong);
        assertArrayEquals(request.getTestBytes(), TestConstants.testBytes);
        counter.getAndIncrement();
      }
    });

    client = ClientBuilder.of(TestConstants.BUILDER, "localhost", 54321);

    average = new AtomicInteger();
    counter = new AtomicInteger();

    assertTrue(server.start());
    assertTrue(client.start());

  }

  @AfterClass
  public static void afterClass() {
    assertTrue(client.stop());
    assertTrue(server.stop());
    System.out.println(average.get() + " packets/sec in average");
    System.out.println("== Finished RandomRequest/Sec Behaviour == ");
  }

  @Test
  public void testStringPerSec() throws Exception {
    Thread.sleep(250);
    final long start = System.nanoTime();
    for (int i = 0; i < amount; i++) {
      client.send(TestConstants.TEST_REQUEST);
    }
    final long end = System.nanoTime();
    final long time = (end - start);
    int packetsPerSec = TestConstants.getPacketsPerSec(amount, time);
    TestConstants.adjustAverage(average, packetsPerSec);
    while (amount > counter.get()) {
      Thread.sleep(200);
      System.out.println("Sleeping.. (" + counter.get() + "/" + amount + ")");
    }
    System.out.println(
        amount + "/" + counter.get() + " successful in " + (time * (1 / 1000000000f)) + " seconds");
    counter.set(0);
  }

}
