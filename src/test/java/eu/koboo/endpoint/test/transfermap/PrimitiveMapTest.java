package eu.koboo.endpoint.test.transfermap;

import eu.koboo.endpoint.core.primitive.PrimitiveMap;
import eu.koboo.endpoint.core.primitive.PrimitiveUtils;
import eu.koboo.endpoint.test.TestConstants;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class PrimitiveMapTest {

    @BeforeClass
    public static void setupClass() {
        System.out.println("== Test PrimitiveMapTest Behaviour == ");
    }

    @Test
    public void test() throws Exception {

        PrimitiveMap map = new PrimitiveMap();

        map.put("testString", TestConstants.testString);
        map.put("testLong", TestConstants.testLong);
        map.put("testBytes", TestConstants.testBytes);

        System.out.println("Initialized PrimitiveMap");

        byte[] encoded = PrimitiveUtils.Stream.encodeStream(map);
        System.out.println("Encoded");

        PrimitiveMap decoded = PrimitiveUtils.Stream.decodeStream(encoded);
        System.out.println("Decoded");

        String testString = decoded.get("testString", String.class);
        long testLong = decoded.get("testLong", Long.class);
        byte[] testBytes = decoded.get("testBytes", byte[].class);

        Assert.assertEquals(testString, TestConstants.testString);
        Assert.assertEquals(testLong, TestConstants.testLong);
        Assert.assertArrayEquals(testBytes, TestConstants.testBytes);
        System.out.println("Asserted against TestConstants");

        byte[] rencoded = PrimitiveUtils.Stream.encodeStream(decoded);
        System.out.println("Encoded/2");

        assertArrayEquals(encoded, rencoded);
        System.out.println("Assert against bytes");
    }

    @AfterClass
    public static void afterClass() {
        System.out.println("== Finished PrimitiveMapTest Behaviour == ");
    }

}
