package eu.koboo.endpoint.transferable.actor;

import eu.koboo.endpoint.transferable.PrimitiveActor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class BooleanActor extends PrimitiveActor<Boolean> {

    @Override
    public void write(DataOutputStream output, Boolean object) throws IOException {
        output.writeBoolean(object);
    }

    @Override
    public Boolean read(DataInputStream input) throws IOException {
        return input.readBoolean();
    }
}
