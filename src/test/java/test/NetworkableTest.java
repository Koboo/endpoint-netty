package test;

import eu.koboo.endpoint.networkable.NetworkableEncoder;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class NetworkableTest {

    public static NetworkableEncoder encoder;

    static NetworkTestObject networkTestObject;
    static byte[] bytes;

    @BeforeClass
    public static void setupClass() {
        encoder = new NetworkableEncoder();
        encoder.register(1, NetworkTestObject::new);
        System.out.println("== Test NetworkableEncoder Behaviour == ");
    }

    @AfterClass
    public static void afterClass() {
        System.out.println("== Finished NetworkableEncoder Behaviour == ");
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

        assertEquals(testObject.getTestString(), TestConstants.testString);
        assertEquals(testObject.getTestLong(), TestConstants.testLong);
        assertArrayEquals(testObject.getTestBytes(), TestConstants.testBytes);
        System.out.println("Asserted against TestConstants");

        assertEquals(testObject.getTestString(), networkTestObject.getTestString());
        assertEquals(testObject.getTestLong(), networkTestObject.getTestLong());
        assertArrayEquals(testObject.getTestBytes(), networkTestObject.getTestBytes());
        System.out.println("Asserted against NetworkTestObject");

        byte[] testBytes = encoder.encode(testObject);
        System.out.println("Encoded no. 2");

        assertArrayEquals(bytes, testBytes);
        System.out.println("Assert against encoded bytes");
    }
}
