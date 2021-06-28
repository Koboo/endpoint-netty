package eu.koboo.endpoint.networkable;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public interface Networkable {

    void readStream(DataInputStream output) throws Exception;

    void writeStream(DataOutputStream input) throws Exception;
}
