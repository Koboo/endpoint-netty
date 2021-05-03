package test;

import eu.koboo.endpoint.core.builder.EndpointBuilder;
import eu.koboo.endpoint.core.builder.param.ErrorMode;
import eu.koboo.endpoint.core.builder.param.EventMode;
import eu.koboo.endpoint.core.builder.param.Protocol;
import test.benchmark.DataRequestTest;
import test.benchmark.NativeRequestTest;
import test.benchmark.RandomRequestTest;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class StaticTest {

    public static EndpointBuilder BUILDER;

    public static RandomRequestTest.RandomRequest RANDOM_REQUEST;
    public static DataRequestTest.DataRequest DATA_REQUEST;
    public static NativeRequestTest.NativeRequest NATIVE_REQUEST;

    public static String testString;
    public static long testLong;
    public static int testInt;
    public static byte[] testBytes;

    static {
        BUILDER = EndpointBuilder.newBuilder()
                .logging(false)
                .protocol(Protocol.SERIALIZABLE)
                .errorMode(ErrorMode.STACK_TRACE)
                .eventMode(EventMode.SERVICE);

        Random random = new Random();
        testBytes = new byte[1000];
        random.nextBytes(testBytes);

        testString = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat";
        testLong = 1_234_567_890;
        testInt = new Random().nextInt(5000);

        RANDOM_REQUEST = new RandomRequestTest.RandomRequest(testString, testLong, testBytes);
        DATA_REQUEST = new DataRequestTest.DataRequest(testString, testLong, testInt);
        NATIVE_REQUEST = new NativeRequestTest.NativeRequest()
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
