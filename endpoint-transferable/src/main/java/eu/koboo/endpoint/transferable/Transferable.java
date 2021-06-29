package eu.koboo.endpoint.transferable;

import java.io.DataInputStream;
import java.io.DataOutputStream;

@SuppressWarnings("all")
public interface Transferable {

    void readStream(DataInputStream input) throws Exception;

    void writeStream(DataOutputStream output) throws Exception;

    default <Prim> void write(DataOutputStream outputStream, Prim object) {
        try {
            if(object instanceof Boolean) {
                outputStream.writeBoolean((Boolean) object);
            } else if(object instanceof Byte) {
                outputStream.writeByte((Byte) object);
            } else if(object instanceof Character) {
                outputStream.writeChar((Character) object);
            } else if(object instanceof Long) {
                outputStream.writeLong((Long) object);
            } else if(object instanceof Float) {
                outputStream.writeFloat((Float) object);
            } else if(object instanceof Short) {
                outputStream.writeShort((Short) object);
            } else if(object instanceof Double) {
                outputStream.writeDouble((Double) object);
            } else if(object instanceof String) {
                outputStream.writeUTF((String) object);
            } else if(object instanceof Integer) {
                outputStream.writeInt((Integer) object);
            } else {
                throw new IllegalStateException("No primitive found! Custom object?");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    default <Prim> Prim read(DataInputStream inputStream, Class<Prim> primClass) {
        try {
            Primitive primitive = Primitive.valueOf(primClass.getSimpleName());

            switch (primitive) {
                case BOOLEAN:
                    return (Prim) Boolean.valueOf(inputStream.readBoolean());
                case BYTE:
                    return (Prim) Byte.valueOf(inputStream.readByte());
                case CHAR:
                    return (Prim) Character.valueOf(inputStream.readChar());
                case LONG:
                    return (Prim) Long.valueOf(inputStream.readLong());
                case FLOAT:
                    return (Prim) Float.valueOf(inputStream.readFloat());
                case SHORT:
                    return (Prim) Short.valueOf(inputStream.readShort());
                case DOUBLE:
                    return (Prim) Double.valueOf(inputStream.readDouble());
                case STRING:
                    return (Prim) inputStream.readUTF();
                case INTEGER:
                    return (Prim) Integer.valueOf(inputStream.readInt());
                default:
                    throw new IllegalStateException("No primitive found! Custom object?");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    default byte[] readArray(DataInputStream input) throws Exception {
        int length = input.readInt();
        byte[] buffer = new byte[length];
        input.read(buffer);
        return buffer;
    }

    default void writeArray(DataOutputStream output, byte[] buffer) throws Exception {
        int length = buffer.length;
        output.writeInt(length);
        output.write(buffer);
    }
}
