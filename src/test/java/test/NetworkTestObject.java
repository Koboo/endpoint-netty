package test;

import eu.koboo.endpoint.networkable.NetworkUtil;
import eu.koboo.endpoint.networkable.Networkable;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class NetworkTestObject extends TestRequest implements Networkable {

    @Override
    public void readStream(DataInputStream input) throws Exception {
        setTestString(input.readUTF());
        setTestLong(input.readLong());
        setTestBytes(NetworkUtil.readArray(input));
    }

    @Override
    public void writeStream(DataOutputStream output) throws Exception {
        output.writeUTF(getTestString());
        output.writeLong(getTestLong());
        NetworkUtil.writeArray(getTestBytes(), output);
    }
}