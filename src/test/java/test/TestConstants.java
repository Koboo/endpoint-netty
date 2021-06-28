package test;

import eu.koboo.endpoint.core.builder.EndpointBuilder;
import eu.koboo.endpoint.core.builder.param.ErrorMode;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class TestConstants {

    public static EndpointBuilder BUILDER;

    public static TestRequest TEST_REQUEST;

    public static String testString;
    public static long testLong;
    public static byte[] testBytes;

    static {
        BUILDER = EndpointBuilder.builder()
                .logging(false)
                .errorMode(ErrorMode.STACK_TRACE)
                .registerPacket(1, TestRequest::new);

        Random random = new Random();
        testBytes = new byte[1000];
        random.nextBytes(testBytes);

        testString = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat";
        testLong = 1_234_567_890;

        TEST_REQUEST = new TestRequest()
                .setTestString(testString)
                .setTestLong(testLong)
                .setTestBytes(testBytes);
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
}
