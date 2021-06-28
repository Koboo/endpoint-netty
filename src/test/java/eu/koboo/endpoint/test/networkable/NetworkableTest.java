package eu.koboo.endpoint.test.networkable;

import eu.koboo.endpoint.networkable.NetworkableEncoder;

import eu.koboo.endpoint.test.TestConstants;
import org.junit.AfterClass;
import org.junit.Assert;
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
}
