package eu.koboo.endpoint.test.transfermap;

import eu.koboo.endpoint.core.transfer.PrimitiveUtils;
import eu.koboo.endpoint.core.transfer.TransferMap;
import eu.koboo.endpoint.test.TestConstants;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class TransferMapTest {

    @BeforeClass
    public static void setupClass() {
        System.out.println("== Test TransferMapTest Behaviour == ");
    }

    @Test
    public void test() throws Exception {

        TransferMap map = new TransferMap();

        map.put("testString", TestConstants.testString);
        map.put("testLong", TestConstants.testLong);
        map.put("testBytes", TestConstants.testBytes);

        System.out.println("Initialized TransferMap");

        byte[] encoded = PrimitiveUtils.encodeByteArray(map);
        System.out.println("Encoded");

        TransferMap decoded = PrimitiveUtils.decodeByteArray(encoded);
        System.out.println("Decoded");

        String testString = decoded.get("testString", String.class);
        long testLong = decoded.get("testLong", Long.class);
        byte[] testBytes = decoded.get("testBytes", byte[].class);

        Assert.assertEquals(testString, TestConstants.testString);
        Assert.assertEquals(testLong, TestConstants.testLong);
        Assert.assertArrayEquals(testBytes, TestConstants.testBytes);
        System.out.println("Asserted against TestConstants");

        byte[] rencoded = PrimitiveUtils.encodeByteArray(decoded);
        System.out.println("Encoded/2");

        assertArrayEquals(encoded, rencoded);
        System.out.println("Assert against bytes");
    }

    @AfterClass
    public static void afterClass() {
        System.out.println("== Finished TransferMapTest Behaviour == ");
    }

}
