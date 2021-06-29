package eu.koboo.endpoint.test.transferable;

import eu.koboo.endpoint.test.TestConstants;
import eu.koboo.endpoint.transferable.TransferCodec;
import eu.koboo.endpoint.transferable.Transferable;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.function.Supplier;

import static org.junit.Assert.assertArrayEquals;

public class TransferableTest {

    public static TransferCodec encoder;

    static TransferObject transferObject;
    static byte[] bytes;

    @BeforeClass
    public static void setupClass() {
        encoder = new TransferCodec();
        encoder.register(1, TransferObject::new);
        System.out.println("== Test TransferableTest Behaviour == ");
    }

    @Test
    public void test() throws Exception {

        transferObject = new TransferObject();
        transferObject.setTestString(TestConstants.testString);
        transferObject.setTestLong(TestConstants.testLong);
        transferObject.setTestBytes(TestConstants.testBytes);

        System.out.println("Initialized TransferObject");

        bytes = encoder.encode(transferObject);
        System.out.println("Encoded TransferObject");

        TransferObject testObject = encoder.decode(bytes);
        System.out.println("Decoded TransferObject");

        Assert.assertEquals(testObject.getTestString(), TestConstants.testString);
        Assert.assertEquals(testObject.getTestLong(), TestConstants.testLong);
        Assert.assertArrayEquals(testObject.getTestBytes(), TestConstants.testBytes);
        System.out.println("Asserted against TestConstants");

        Assert.assertEquals(testObject.getTestString(), transferObject.getTestString());
        Assert.assertEquals(testObject.getTestLong(), transferObject.getTestLong());
        Assert.assertArrayEquals(testObject.getTestBytes(), transferObject.getTestBytes());
        System.out.println("Asserted against TransferObject");

        byte[] testBytes = encoder.encode(testObject);
        System.out.println("Encoded no. 2");

        assertArrayEquals(bytes, testBytes);
        System.out.println("Assert against encoded bytes");
    }

    @AfterClass
    public static void afterClass() {
        System.out.println("== Finished TransferableTest Behaviour == ");
    }

    public static void main(String[] args) {
        TransferCodec transferCodec = new TransferCodec();
        transferCodec
                .register(1, new Supplier<Transferable>() {
                    @Override
                    public Transferable get() {
                        return new TransferObject();
                    }
                })
                .register(2, TransferObject::new);

        byte[] objectEncoded = transferCodec.encode(transferObject);

        TransferObject objectDecoded = transferCodec.decode(objectEncoded);

    }
}
