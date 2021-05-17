
package test.benchmark;

import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import eu.binflux.serial.core.SerializerPool;
import eu.binflux.serial.fst.FSTSerialization;
import eu.koboo.endpoint.client.EndpointClient;
import eu.koboo.endpoint.core.protocols.serializable.SerializablePacket;
import eu.koboo.endpoint.core.protocols.serializable.SerializableReceiveEvent;
import eu.koboo.endpoint.server.EndpointServer;
import eu.koboo.event.listener.EventListener;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import test.StaticTest;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class RandomRequestTest extends AbstractBenchmark {

    public static EndpointServer server;
    public static EndpointClient client;

    public static AtomicInteger counter;
    public static AtomicInteger average;

    @BeforeClass
    public static void setupClass() {
        System.out.println("== Test RandomRequest/Sec Behaviour == ");

        StaticTest.BUILDER.serializer(new SerializerPool(FSTSerialization.class));

        server = new EndpointServer(StaticTest.BUILDER, 54321);
        server.eventHandler().register(new EventListener<SerializableReceiveEvent>() {
            @Override
            public void onEvent(SerializableReceiveEvent event) {
                if(event.getTypeObject() instanceof RandomRequest) {
                    RandomRequest request = (RandomRequest) event.getTypeObject();
                    assertEquals(request.getTestString(), StaticTest.testString);
                    assertEquals(request.getTestLong(), StaticTest.testLong);
                    assertArrayEquals(request.getTestBytes(), StaticTest.testBytes);
                    counter.getAndIncrement();
                }
            }
        });

        client = new EndpointClient(StaticTest.BUILDER, "localhost", 54321);

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
        int amount = 5_000;
        for (int i = 0; i < amount; i++) {
            client.send(StaticTest.RANDOM_REQUEST);
        }
        final long end = System.nanoTime();
        final long time = (end - start);
        int packetsPerSec = StaticTest.getPacketsPerSec(amount, time);
        StaticTest.adjustAverage(average, packetsPerSec);
        while(amount > counter.get()) {
            Thread.sleep(200);
            System.out.println("Sleeping.. (" + counter.get() + "/" + amount + ")");
        }
        System.out.println(amount + "/" + counter.get() + " successful in " + (time * (1 / 1000000000f)) + " seconds");
        System.out.println(packetsPerSec + " packets/sec");
        counter.set(0);
    }

    public static class RandomRequest implements SerializablePacket {

        String testString;
        long testLong;
        byte[] testBytes;

        public RandomRequest() {
        }

        public RandomRequest(String testString, long testLong, byte[] testBytes) {
            this.testString = testString;
            this.testLong = testLong;
            this.testBytes = testBytes;
        }

        public String getTestString() {
            return testString;
        }

        public long getTestLong() {
            return testLong;
        }

        public byte[] getTestBytes() {
            return testBytes;
        }

    }

}
