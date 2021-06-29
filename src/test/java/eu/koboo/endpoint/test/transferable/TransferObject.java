package eu.koboo.endpoint.test.transferable;

import eu.koboo.endpoint.transferable.Transferable;
import eu.koboo.endpoint.test.TestRequest;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class TransferObject extends TestRequest implements Transferable {

    @Override
    public void readStream(DataInputStream input) throws Exception {
        setTestString(read(input, String.class));
        setTestLong(read(input, Long.class));
        setTestBytes(readArray(input));
    }

    @Override
    public void writeStream(DataOutputStream output) throws Exception {
        write(output, getTestString());
        write(output, getTestLong());
        writeArray(output, getTestBytes());
    }
}