package eu.koboo.endpoint.networkable;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public interface Networkable {

    void readStream(DataInputStream input) throws Exception;

    void writeStream(DataOutputStream output) throws Exception;
}
