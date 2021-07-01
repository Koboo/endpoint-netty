package eu.koboo.endpoint.test.transfermap;

import eu.koboo.endpoint.core.transfer.Primitive;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PrimitiveOfTest {

    @BeforeClass
    public static void setupClass() {
        System.out.println("== Test PrimitiveOfTest Behaviour == ");
    }

    @Test
    public void test() throws Exception {
        double testDouble = 5.0D;
        Primitive primitive = Primitive.of(testDouble);
        assertEquals(primitive, Primitive.DOUBLE);

        double[] testDoubleArray = new double[]{1, 2, 3, 4, 5};
        primitive = Primitive.of(testDoubleArray);
        assertEquals(primitive, Primitive.DOUBLE_ARRAY);

        char testChar = 'F';
        primitive = Primitive.of(testChar);
        assertEquals(primitive, Primitive.CHAR);

        char[] testCharArray = new char[]{'A', 'B', 'C', 'D'};
        primitive = Primitive.of(testCharArray);
        assertEquals(primitive, Primitive.CHAR_ARRAY);

        Character character = 'B';
        primitive = Primitive.of(character);
        assertEquals(primitive, Primitive.CHAR);
    }

    @AfterClass
    public static void afterClass() {
        System.out.println("== Finished PrimitiveOfTest Behaviour == ");
    }

}
