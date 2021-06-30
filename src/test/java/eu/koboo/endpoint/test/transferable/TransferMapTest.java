package eu.koboo.endpoint.test.transferable;

import eu.koboo.endpoint.test.TestConstants;
import eu.koboo.endpoint.transferable.TransferCodec;
import eu.koboo.endpoint.transferable.TransferMap;
import eu.koboo.endpoint.transferable.Transferable;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.function.Supplier;

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

        System.out.println("Initialized TransferMap");

        byte[] encoded = TransferMap.encode(map);
        System.out.println("Encoded");

        assert encoded != null;

        TransferMap decoded = TransferMap.decode(encoded);
        System.out.println("Decoded");

        assert decoded != null;

        String testString = decoded.get("testString", String.class);
        long testLong = decoded.get("testLong", Long.class);

        Assert.assertEquals(testString, TestConstants.testString);
        Assert.assertEquals(testLong, TestConstants.testLong);
        System.out.println("Asserted against TestConstants");

        byte[] rencoded = TransferMap.encode(decoded);
        System.out.println("Encoded/2");

        assertArrayEquals(encoded, rencoded);
        System.out.println("Assert against bytes");
    }

    @AfterClass
    public static void afterClass() {
        System.out.println("== Finished TransferMapTest Behaviour == ");
    }

}
