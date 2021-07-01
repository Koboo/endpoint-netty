package eu.koboo.endpoint.test;

import eu.koboo.endpoint.core.builder.EndpointBuilder;
import eu.koboo.endpoint.core.builder.param.ErrorMode;
import eu.koboo.endpoint.core.transfer.TransferMap;
import eu.koboo.endpoint.core.transfer.TransferMapPacket;
import org.junit.Assert;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class TestConstants {

    public static EndpointBuilder BUILDER;

    public static TestRequest TEST_REQUEST;

    public static TransferMap TEST_TRANSFER_MAP;
    public static TransferMapPacket TEST_MAP_PACKET;

    public static String testString;
    public static long testLong;
    public static byte[] testBytes;

    static {
        BUILDER = EndpointBuilder.builder()
                .framing(true)
                .processing(true)
                .logging(false)
                .errorMode(ErrorMode.STACK_TRACE)
                .registerPacket(1, TestRequest::new);

        Random random = new Random();
        testBytes = new byte[8192];
        random.nextBytes(testBytes);

        testString = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat";
        testLong = 1_234_567_890;

        TEST_REQUEST = new TestRequest()
                .setTestString(testString)
                .setTestLong(testLong)
                .setTestBytes(testBytes);

        TEST_TRANSFER_MAP = new TransferMap();

        TEST_TRANSFER_MAP.put("testString", TestConstants.testString);
        TEST_TRANSFER_MAP.put("testLong", TestConstants.testLong);
        TEST_TRANSFER_MAP.put("testBytes", TestConstants.testBytes);

        TEST_MAP_PACKET = new TransferMapPacket();
        TEST_MAP_PACKET.setTransferMap(TEST_TRANSFER_MAP);
    }

    public static int getPacketsPerSec(int amount, long time) {
        return Math.round(amount / (time * (1 / 1000000000f)));
    }

    public static void adjustAverage(AtomicInteger average, int packetsPerSec) {
        if (average.get() != 0) {
            average.set(Math.round(packetsPerSec + average.get()) / 2);
        } else {
            average.set(packetsPerSec);
        }
    }

    public static void printByteArray(String prefix, byte[] bytes) {
        StringBuilder setBuilder = new StringBuilder();
        if (bytes != null) {
            for (byte cont : bytes) {
                setBuilder.append(cont).append(" ");
            }
        }
        System.out.println(prefix + setBuilder.toString());
    }

    public static void assertRequest(TestRequest request) {
        assertEquals(request.getTestString(), TestConstants.testString);
        assertEquals(request.getTestLong(), TestConstants.testLong);
        assertArrayEquals(request.getTestBytes(), TestConstants.testBytes);
    }

    public static void assertMap(TransferMapPacket transferMapPacket) {
        TransferMap transferMap = transferMapPacket.getTransferMap();
        Assert.assertNotNull(transferMap);
        String testString = transferMap.get("testString", String.class);
        long testLong = transferMap.get("testLong", Long.class);
        byte[] testBytes = transferMap.get("testBytes", byte[].class);
        Assert.assertEquals(testString, TestConstants.testString);
        Assert.assertEquals(testLong, TestConstants.testLong);
        Assert.assertArrayEquals(testBytes, TestConstants.testBytes);
    }


}
