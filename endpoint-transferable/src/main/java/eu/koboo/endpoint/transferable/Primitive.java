package eu.koboo.endpoint.transferable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum Primitive {


    BOOLEAN(Boolean.class, boolean.class),
    BYTE(Byte.class, byte.class),
    CHAR(Character.class, char.class),
    FLOAT(Float.class, float.class),
    DOUBLE(Double.class, double.class),
    SHORT(Short.class, short.class),
    LONG(Long.class, long.class),
    INTEGER(Integer.class, int.class),
    STRING(String.class, String.class),

    BOOLEAN_ARRAY(Boolean[].class, boolean[].class),
    BYTE_ARRAY(Byte[].class, byte[].class),
    CHAR_ARRAY(Character[].class, char[].class),
    FLOAT_ARRAY(Float[].class, float[].class),
    DOUBLE_ARRAY(Double[].class, double[].class),
    SHORT_ARRAY(Short[].class, short[].class),
    LONG_ARRAY(Long[].class, long[].class),
    INTEGER_ARRAY(Integer[].class, int[].class),
    STRING_ARRAY(String[].class, String[].class);


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

    Primitive(Class<?> primitiveClass, Class<?> nativeClass) {
        this.primitiveClass = primitiveClass;
        this.nativeClass = nativeClass;
    }

    public static Primitive of(Object object) {
        return PRIMITIVE_REGISTRY.get(object.getClass().getSimpleName());
    }

    public static boolean isPrimitive(Object object) {
        return PRIMITIVE_REGISTRY.containsKey(object.getClass().getSimpleName());
    }

    public static <Prim> void writeDynamic(DataOutputStream outputStream, Prim object) {
        try {
            Primitive primitive = Primitive.of(object);
            int len;
            switch (primitive) {
                case BOOLEAN:
                    boolean bool = (Boolean) object;
                    outputStream.writeBoolean(bool);
                    break;
                case BOOLEAN_ARRAY:
                    boolean[] bools = (boolean[]) object;
                    len = bools.length;
                    outputStream.writeInt(len);
                    for (boolean b : bools) {
                        outputStream.writeBoolean(b);
                    }
                    break;
                case INTEGER:
                    int x = (Integer) object;
                    outputStream.writeInt(x);
                    break;
                case INTEGER_ARRAY:
                    int[] ints = (int[]) object;
                    len = ints.length;
                    outputStream.writeInt(len);
                    for (int i : ints) {
                        outputStream.writeInt(i);
                    }
                    break;
                case STRING:
                    String string = (String) object;
                    outputStream.writeUTF(string);
                    break;
                case STRING_ARRAY:
                    String[] strings = (String[]) object;
                    len = strings.length;
                    outputStream.writeInt(len);
                    for (String s : strings) {
                        outputStream.writeUTF(s);
                    }
                    break;
                case DOUBLE:
                    double dou = (Double) object;
                    outputStream.writeDouble(dou);
                    break;
                case DOUBLE_ARRAY:
                    double[] doubles = (double[]) object;
                    len = doubles.length;
                    outputStream.writeInt(len);
                    for (double d : doubles) {
                        outputStream.writeDouble(d);
                    }
                    break;
                case SHORT:
                    short sho = (Short) object;
                    outputStream.writeShort(sho);
                    break;
                case SHORT_ARRAY:
                    short[] shorts = (short[]) object;
                    len = shorts.length;
                    outputStream.writeInt(len);
                    for (short s : shorts) {
                        outputStream.writeShort(s);
                    }
                    break;
                case FLOAT:
                    float flo = (Float) object;
                    outputStream.writeFloat(flo);
                    break;
                case FLOAT_ARRAY:
                    float[] floats = (float[]) object;
                    len = floats.length;
                    outputStream.writeInt(len);
                    for (float f : floats) {
                        outputStream.writeFloat(f);
                    }
                    break;
                case LONG:
                    long lo = (Long) object;
                    outputStream.writeLong(lo);
                    break;
                case LONG_ARRAY:
                    long[] longs = (long[]) object;
                    len = longs.length;
                    outputStream.writeInt(len);
                    for (long l : longs) {
                        outputStream.writeLong(l);
                    }
                    break;
                case CHAR:
                    char ch = (Character) object;
                    outputStream.writeChar(ch);
                    break;
                case CHAR_ARRAY:
                    char[] chars = (char[]) object;
                    len = chars.length;
                    outputStream.writeInt(len);
                    for (char c : chars) {
                        outputStream.writeChar(c);
                    }
                    break;
                case BYTE:
                    byte by = (Byte) object;
                    outputStream.writeByte(by);
                    break;
                case BYTE_ARRAY:
                    byte[] bytes = (byte[]) object;
                    len = bytes.length;
                    outputStream.writeInt(len);
                    for (byte b : bytes) {
                        outputStream.writeByte(b);
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static <Prim> Prim readDynamic(DataInputStream inputStream, Primitive primitive) {
        try {
            int len;
            switch (primitive) {
                case BOOLEAN:
                    return (Prim) Boolean.valueOf(inputStream.readBoolean());
                case BOOLEAN_ARRAY:
                    len = inputStream.readInt();
                    boolean[] bools = new boolean[len];
                    for (int i = 0; i < len; i++) {
                        bools[i] = inputStream.readBoolean();
                    }
                    return (Prim) bools;
                case INTEGER:
                    return (Prim) Integer.valueOf(inputStream.readInt());
                case INTEGER_ARRAY:
                    len = inputStream.readInt();
                    int[] ints = new int[len];
                    for (int i = 0; i < len; i++) {
                        ints[i] = inputStream.readInt();
                    }
                    return (Prim) ints;
                case STRING:
                    return (Prim) inputStream.readUTF();
                case STRING_ARRAY:
                    len = inputStream.readInt();
                    String[] strings = new String[len];
                    for (int i = 0; i < len; i++) {
                        strings[i] = inputStream.readUTF();
                    }
                    return (Prim) strings;
                case DOUBLE:
                    return (Prim) Double.valueOf(inputStream.readDouble());
                case DOUBLE_ARRAY:
                    len = inputStream.readInt();
                    double[] doubles = new double[len];
                    for (int i = 0; i < len; i++) {
                        doubles[i] = inputStream.readDouble();
                    }
                    return (Prim) doubles;
                case SHORT:
                    return (Prim) Short.valueOf(inputStream.readShort());
                case SHORT_ARRAY:
                    len = inputStream.readInt();
                    short[] shorts = new short[len];
                    for (int i = 0; i < len; i++) {
                        shorts[i] = inputStream.readShort();
                    }
                    return (Prim) shorts;
                case FLOAT:
                    return (Prim) Float.valueOf(inputStream.readFloat());
                case FLOAT_ARRAY:
                    len = inputStream.readInt();
                    float[] floats = new float[len];
                    for (int i = 0; i < len; i++) {
                        floats[i] = inputStream.readFloat();
                    }
                    return (Prim) floats;
                case LONG:
                    return (Prim) Long.valueOf(inputStream.readLong());
                case LONG_ARRAY:
                    len = inputStream.readInt();
                    long[] longs = new long[len];
                    for (int i = 0; i < len; i++) {
                        longs[i] = inputStream.readLong();
                    }
                    return (Prim) longs;
                case CHAR:
                    return (Prim) Character.valueOf(inputStream.readChar());
                case CHAR_ARRAY:
                    len = inputStream.readInt();
                    char[] chars = new char[len];
                    for (int i = 0; i < len; i++) {
                        chars[i] = inputStream.readChar();
                    }
                    return (Prim) chars;
                case BYTE:
                    return (Prim) Byte.valueOf(inputStream.readByte());
                case BYTE_ARRAY:
                    len = inputStream.readInt();
                    byte[] bytes = new byte[len];
                    for (int i = 0; i < len; i++) {
                        bytes[i] = inputStream.readByte();
                    }
                    return (Prim) bytes;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}