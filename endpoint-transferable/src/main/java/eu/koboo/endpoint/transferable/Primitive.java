package eu.koboo.endpoint.transferable;

import eu.koboo.endpoint.transferable.actor.BooleanActor;
import eu.koboo.endpoint.transferable.actor.BooleanArrayActor;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum Primitive {

    BOOLEAN(Boolean.class, boolean.class, new BooleanActor()),
    BYTE(Byte.class, byte.class, null),
    CHAR(Character.class, char.class, null),
    FLOAT(Float.class, float.class, null),
    DOUBLE(Double.class, double.class, null),
    SHORT(Short.class, short.class, null),
    LONG(Long.class, long.class, null),
    INTEGER(Integer.class, int.class, null),
    STRING(String.class, String.class, null),

    BOOLEAN_ARRAY(Boolean[].class, boolean[].class, new BooleanArrayActor()),
    BYTE_ARRAY(Byte[].class, byte[].class, null),
    CHAR_ARRAY(Character[].class, char[].class, null),
    FLOAT_ARRAY(Float[].class, float[].class, null),
    DOUBLE_ARRAY(Double[].class, double[].class, null),
    SHORT_ARRAY(Short[].class, short[].class, null),
    LONG_ARRAY(Long[].class, long[].class, null),
    INTEGER_ARRAY(Integer[].class, int[].class, null),
    STRING_ARRAY(String[].class, String[].class, null);

    public static final Primitive[] PRIMITIVES = Primitive.values();
    private static final Map<String, Primitive> PRIMITIVE_REGISTRY = new ConcurrentHashMap<>();

    static {
        for (Primitive primitive : PRIMITIVES) {
            PRIMITIVE_REGISTRY.put(primitive.primitiveClass.getSimpleName(), primitive);
            PRIMITIVE_REGISTRY.put(primitive.nativeClass.getSimpleName(), primitive);
        }
    }

    private final Class<?> primitiveClass;
    private final Class<?> nativeClass;
    private final PrimitiveActor<?> actor;
    private final boolean array;

    Primitive(Class<?> primitiveClass, Class<?> nativeClass, PrimitiveActor<?> actor) {
        this.primitiveClass = primitiveClass;
        this.nativeClass = nativeClass;
        this.array = name().endsWith("_ARRAY");
        this.actor = actor;
    }

    public Class<?> getPrimitiveClass() {
        return primitiveClass;
    }

    public Class<?> getNativeClass() {
        return nativeClass;
    }

    public static Primitive of(Object object) {
        return PRIMITIVE_REGISTRY.get(object.getClass().getSimpleName());
    }

    public static boolean isPrimitive(Object object) {
        return PRIMITIVE_REGISTRY.containsKey(object.getClass().getSimpleName());
    }

    public static <Prim> void write(DataOutputStream outputStream, Prim object) {
        try {
            if (object instanceof Boolean) {
                outputStream.writeBoolean((Boolean) object);
            } else if (object instanceof Byte) {
                outputStream.writeByte((Byte) object);
            } else if (object instanceof Character) {
                outputStream.writeChar((Character) object);
            } else if (object instanceof Long) {
                outputStream.writeLong((Long) object);
            } else if (object instanceof Float) {
                outputStream.writeFloat((Float) object);
            } else if (object instanceof Short) {
                outputStream.writeShort((Short) object);
            } else if (object instanceof Double) {
                outputStream.writeDouble((Double) object);
            } else if (object instanceof String) {
                outputStream.writeUTF((String) object);
            } else if (object instanceof Integer) {
                outputStream.writeInt((Integer) object);
            } else {
                throw new IllegalStateException("No primitive! '" + object.getClass().getName() + "' is not a primitive type!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static <Prim> Prim read(DataInputStream inputStream, Class<Prim> primClass) {
        try {
            String className = primClass.getSimpleName();
            if (className.equals(Boolean.class.getSimpleName())) {
                return (Prim) Boolean.valueOf(inputStream.readBoolean());
            } else if (className.equals(Byte.class.getSimpleName())) {
                return (Prim) Byte.valueOf(inputStream.readByte());
            } else if (className.equals(Character.class.getSimpleName())) {
                return (Prim) Character.valueOf(inputStream.readChar());
            } else if (className.equals(Long.class.getSimpleName())) {
                return (Prim) Long.valueOf(inputStream.readLong());
            } else if (className.equals(Float.class.getSimpleName())) {
                return (Prim) Float.valueOf(inputStream.readFloat());
            } else if (className.equals(Short.class.getSimpleName())) {
                return (Prim) Short.valueOf(inputStream.readShort());
            } else if (className.equals(Double.class.getSimpleName())) {
                return (Prim) Double.valueOf(inputStream.readDouble());
            } else if (className.equals(Integer.class.getSimpleName())) {
                return (Prim) Integer.valueOf(inputStream.readInt());
            } else if (className.equals(String.class.getSimpleName())) {
                return (Prim) inputStream.readUTF();
            }
            throw new IllegalStateException("No primitive! '" + primClass.getName() + "' is not a primitive type!");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(buffer);

        boolean testBool = true;

        Primitive primitive = Primitive.of(testBool);
        primitive.actor.write(outputStream, testBool);

        boolean[] testBools = new boolean[]{true, false, true, false, false, false};


    }
}