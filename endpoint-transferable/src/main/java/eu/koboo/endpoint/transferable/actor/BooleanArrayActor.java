package eu.koboo.endpoint.transferable.actor;

import eu.koboo.endpoint.transferable.PrimitiveActor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class BooleanArrayActor extends PrimitiveActor<boolean[]> {

    @Override
    public void write(DataOutputStream output, boolean[] object) throws IOException {
        output.writeInt(object.length);
        for(int i = 0, len = object.length; i < len; i++) {
            output.writeBoolean(object[i]);
        }
    }

    @Override
    public boolean[] read(DataInputStream input) throws IOException {
        int len = input.readInt();
        boolean[] booleanArray = new boolean[len];
        for(int i = 0; i < len; i++) {
            booleanArray[i] = input.readBoolean();
        }
        return booleanArray;
    }
}
