package eu.koboo.endpoint.test.networkable;

import eu.koboo.endpoint.networkable.Networkable;
import eu.koboo.endpoint.test.TestRequest;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class NetworkTestObject extends TestRequest implements Networkable {

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