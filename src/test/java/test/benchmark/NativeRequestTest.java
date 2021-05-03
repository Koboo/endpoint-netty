
package test.benchmark;

import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import eu.binflux.serial.core.SerializerPool;
import eu.binflux.serial.fst.FSTSerialization;
import eu.koboo.endoint.client.EndpointClient;
import eu.koboo.endpoint.core.builder.param.Protocol;
import eu.koboo.endpoint.core.protocols.natives.NativePacket;
import eu.koboo.endpoint.core.protocols.natives.NativeReceiveEvent;
import eu.koboo.endpoint.server.EndpointServer;
import eu.koboo.event.listener.EventListener;
import eu.koboo.nettyutils.BufUtils;
import io.netty.buffer.ByteBuf;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import test.StaticTest;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class NativeRequestTest extends AbstractBenchmark {

    public static EndpointServer server;
    public static EndpointClient client;

    public static AtomicInteger counter;
    public static AtomicInteger average;

    @BeforeClass
    public static void setupClass() {
        System.out.println("== Test NativeRequest/Sec Behaviour == ");

        StaticTest.BUILDER
                .protocol(Protocol.NATIVE_NETTY);

        server = new EndpointServer(StaticTest.BUILDER, 54321);
        server.eventHandler().register(new EventListener<NativeReceiveEvent>() {
            @Override
            public void onEvent(NativeReceiveEvent event) {
                if(event.getTypeObject() instanceof NativeRequest) {
                    NativeRequest request = (NativeRequest) event.getTypeObject();
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
        System.out.println("== Finished NativeRequest/Sec Behaviour == ");
    }

    @Test
    public void testPerSec() throws Exception {
        Thread.sleep(250);
        final long start = System.nanoTime();
        int amount = 5_000;
        for (int i = 0; i < amount; i++) {
            client.send(StaticTest.NATIVE_REQUEST);
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

    public static class NativeRequest implements NativePacket {

        String testString;
        long testLong;
        byte[] testBytes;

        public String getTestString() {
            return testString;
        }

        public NativeRequest setTestString(String testString) {
            this.testString = testString;
            return this;
        }

        public long getTestLong() {
            return testLong;
        }

        public NativeRequest setTestLong(long testLong) {
            this.testLong = testLong;
            return this;
        }

        public byte[] getTestBytes() {
            return testBytes;
        }

        public NativeRequest setTestBytes(byte[] testBytes) {
            this.testBytes = testBytes;
            return this;
        }

        @Override
        public void read(ByteBuf byteBuf) {
            this.testString = BufUtils.readString(byteBuf);
            this.testLong = BufUtils.readVarLong(byteBuf);
            this.testBytes = BufUtils.readArray(byteBuf);
        }

        @Override
        public void write(ByteBuf byteBuf) {
            BufUtils.writeString(testString, byteBuf);
            BufUtils.writeVarLong(testLong, byteBuf);
            BufUtils.writeArray(testBytes, byteBuf);
        }
    }

}
