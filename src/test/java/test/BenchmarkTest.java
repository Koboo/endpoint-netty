
package test;

import eu.koboo.endpoint.client.EndpointClient;
import eu.koboo.endpoint.core.events.ReceiveEvent;
import eu.koboo.endpoint.server.EndpointServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class BenchmarkTest {

    public static EndpointServer server;
    public static EndpointClient client;

    public static AtomicInteger counter;
    public static AtomicInteger average;

    private static final int amount = 5_000;

    @BeforeClass
    public static void setupClass() {
        System.out.println("== Test RandomRequest/Sec Behaviour == ");

        server = new EndpointServer(TestConstants.BUILDER, 54321);
        server.eventHandler().register(ReceiveEvent.class, event -> {
            if (event.getTypeObject() instanceof TestRequest) {
                TestRequest request = event.getTypeObject();
                assertEquals(request.getTestString(), TestConstants.testString);
                assertEquals(request.getTestLong(), TestConstants.testLong);
                assertArrayEquals(request.getTestBytes(), TestConstants.testBytes);
                counter.getAndIncrement();
            }
        });

        client = new EndpointClient(TestConstants.BUILDER, "localhost", 54321);

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
        System.out.println(amount + "/" + counter.get() + " successful in " + (time * (1 / 1000000000f)) + " seconds");
        System.out.println(packetsPerSec + " packets/sec");
        counter.set(0);
    }

}
