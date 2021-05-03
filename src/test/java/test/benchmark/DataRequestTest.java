
package test.benchmark;

import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import eu.binflux.serial.core.SerializerPool;
import eu.binflux.serial.fst.FSTSerialization;
import eu.koboo.endoint.client.EndpointClient;
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

public class DataRequestTest extends AbstractBenchmark {

    public static EndpointServer server;
    public static EndpointClient client;

    public static AtomicInteger counter;
    public static AtomicInteger average;

    @BeforeClass
    public static void setupClass() {
        System.out.println("== Test DataRequest/Sec Behaviour == ");

        StaticTest.BUILDER
                .serializer(new SerializerPool(FSTSerialization.class));

        server = new EndpointServer(StaticTest.BUILDER, 54321);
        server.eventHandler().register(new EventListener<SerializableReceiveEvent>() {
            @Override
            public void onEvent(SerializableReceiveEvent event) {
                if(event.getTypeObject() instanceof DataRequest) {
                    DataRequest request = (DataRequest) event.getTypeObject();
                    assertEquals(request.getString(), StaticTest.testString);
                    assertEquals(request.gettLong(), StaticTest.testLong);
                    assertEquals(request.gettInt(), StaticTest.testInt);
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
        System.out.println("== Finished DataRequest/Sec Behaviour == ");
    }

    @Test
    public void testStringPerSec() throws Exception {
        Thread.sleep(250);
        final long start = System.nanoTime();
        int amount = 5_000;
        for (int i = 0; i < amount; i++) {
            client.send(StaticTest.DATA_REQUEST);
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


    public static class DataRequest implements SerializablePacket {

        private final String string;
        private final long tLong;
        private final int tInt;

        public DataRequest(String string, long tLong, int tInt) {
            this.string = string;
            this.tLong = tLong;
            this.tInt = tInt;
        }

        public String getString() {
            return string;
        }

        public long gettLong() {
            return tLong;
        }

        public int gettInt() {
            return tInt;
        }
    }
}
