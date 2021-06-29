package eu.koboo.endpoint.test.networkable;

import eu.koboo.endpoint.networkable.NetworkCodec;
import eu.koboo.endpoint.networkable.Networkable;

import eu.koboo.endpoint.test.TestConstants;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.function.Supplier;

import static org.junit.Assert.*;

public class NetworkableTest {

    public static NetworkCodec encoder;

    static NetworkTestObject networkTestObject;
    static byte[] bytes;

    @BeforeClass
    public static void setupClass() {
        encoder = new NetworkCodec();
        encoder.register(1, NetworkTestObject::new);
        System.out.println("== Test NetworkableTest Behaviour == ");
    }

    @Test
    public void test() throws Exception {

        networkTestObject = new NetworkTestObject();
        networkTestObject.setTestString(TestConstants.testString);
        networkTestObject.setTestLong(TestConstants.testLong);
        networkTestObject.setTestBytes(TestConstants.testBytes);

        System.out.println("Initialized NetworkTestObject");

        bytes = encoder.encode(networkTestObject);
        System.out.println("Encoded NetworkTestObject");

        NetworkTestObject testObject = encoder.decode(bytes);
        System.out.println("Decoded NetworkTestObject");

        Assert.assertEquals(testObject.getTestString(), TestConstants.testString);
        Assert.assertEquals(testObject.getTestLong(), TestConstants.testLong);
        Assert.assertArrayEquals(testObject.getTestBytes(), TestConstants.testBytes);
        System.out.println("Asserted against TestConstants");

        Assert.assertEquals(testObject.getTestString(), networkTestObject.getTestString());
        Assert.assertEquals(testObject.getTestLong(), networkTestObject.getTestLong());
        Assert.assertArrayEquals(testObject.getTestBytes(), networkTestObject.getTestBytes());
        System.out.println("Asserted against NetworkTestObject");

        byte[] testBytes = encoder.encode(testObject);
        System.out.println("Encoded no. 2");

        assertArrayEquals(bytes, testBytes);
        System.out.println("Assert against encoded bytes");
    }

    @AfterClass
    public static void afterClass() {
        System.out.println("== Finished NetworkableTest Behaviour == ");
    }

    public static void main(String[] args) {
        NetworkCodec networkCodec = new NetworkCodec();
        networkCodec
                .register(1, new Supplier<Networkable>() {
                    @Override
                    public Networkable get() {
                        return new NetworkTestObject();
                    }
                })
                .register(2, NetworkTestObject::new);

        byte[] objectEncoded = networkCodec.encode(networkTestObject);

        NetworkTestObject objectDecoded = networkCodec.decode(objectEncoded);

    }
}
