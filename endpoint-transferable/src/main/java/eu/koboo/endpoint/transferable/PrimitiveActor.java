package eu.koboo.endpoint.transferable;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public abstract class PrimitiveActor<Type> {

    public abstract void write(DataOutputStream output, Type object) throws Exception;

    public abstract Type read(DataInputStream input) throws Exception;

}
