package eu.koboo.endpoint.networkable;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class NetworkUtil {

    public static byte[] readArray(DataInputStream input) throws Exception {
        int length = input.readInt();
        byte[] buffer = new byte[length];
        input.read(buffer);
        return buffer;
    }

    public static void writeArray(byte[] buffer, DataOutputStream output) throws Exception {
        int length = buffer.length;
        output.writeInt(length);
        output.write(buffer);
    }
}
