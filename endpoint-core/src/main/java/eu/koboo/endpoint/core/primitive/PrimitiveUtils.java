package eu.koboo.endpoint.core.primitive;

import eu.koboo.endpoint.core.util.BufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("all")
public class PrimitiveUtils {

    public static class Stream {

        public static byte[] encodeStream(PrimitiveMap primitiveMap) {
            try {
                if (primitiveMap != null) {
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    DataOutputStream outputStream = new DataOutputStream(buffer);
                    int keyLength = primitiveMap.keySet().size();
                    outputStream.writeInt(keyLength);
                    for (Map.Entry<String, Object> entry : primitiveMap.entrySet()) {
                        String key = entry.getKey();
                        Object object = entry.getValue();
                        Primitive primitive = Primitive.of(object);
                        if (primitive == null) {
                            throw new IllegalArgumentException("Value " + object.getClass().getName() + " is not a primitive type!");
                        }
                        outputStream.writeUTF(primitive.name());
                        outputStream.writeUTF(key);
                        writeStream(outputStream, object);
                    }
                    buffer.close();
                    return buffer.toByteArray();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new byte[0];
        }

        public static PrimitiveMap decodeStream(byte[] bytes) {
            PrimitiveMap primitiveMap = new PrimitiveMap();
            try {
                ByteArrayInputStream buffer = new ByteArrayInputStream(bytes);
                DataInputStream inputStream = new DataInputStream(buffer);
                int keyLength = inputStream.readInt();
                for (int i = 0; i < keyLength; i++) {
                    Primitive primitive = Primitive.valueOf(inputStream.readUTF());
                    String key = inputStream.readUTF();
                    Object object = readStream(inputStream, primitive);
                    primitiveMap.put(key, object);
                }
                return primitiveMap;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new PrimitiveMap();
        }

        public static <Prim> void writeStream(DataOutputStream outputStream, Prim object) {
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
                    case UUID:
                        UUID uuid = (UUID) object;
                        outputStream.writeUTF(uuid.toString());
                        break;
                    case UUID_ARRAY:
                        UUID[] uuids = (UUID[]) object;
                        len = uuids.length;
                        outputStream.writeInt(len);
                        for(UUID u : uuids) {
                            outputStream.writeUTF(u.toString());
                        }
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static <Prim> Prim readStream(DataInputStream inputStream, Primitive primitive) {
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
                    case UUID:
                        return (Prim) UUID.fromString(inputStream.readUTF());
                    case UUID_ARRAY:
                        len = inputStream.readInt();
                        UUID[] uuids = new UUID[len];
                        for(int i = 0; i < len; i++) {
                            uuids[i] = UUID.fromString(inputStream.readUTF());
                        }
                        return (Prim) uuids;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

    }

    public static class Buf {

        public static ByteBuf encodeByteBuf(PrimitiveMap primitiveMap) {
            ByteBuf byteBuf = Unpooled.directBuffer();
            int keyLength = primitiveMap.keySet().size();
            BufUtils.writeVarInt(keyLength, byteBuf);
            for (Map.Entry<String, Object> entry : primitiveMap.entrySet()) {
                String key = entry.getKey();
                Object object = entry.getValue();
                Primitive primitive = Primitive.of(object);
                if (primitive == null) {
                    throw new IllegalArgumentException("Value " + object.getClass().getName() + " is not a primitive type!");
                }
                BufUtils.writeString(primitive.name(), byteBuf);
                BufUtils.writeString(key, byteBuf);
                PrimitiveUtils.Buf.writeByteBuf(byteBuf, object);
            }
            return byteBuf;
        }

        public static PrimitiveMap decodeByteBuf(ByteBuf byteBuf) {
            PrimitiveMap primitiveMap = new PrimitiveMap();
            int keyLength = BufUtils.readVarInt(byteBuf);
            for (int i = 0; i < keyLength; i++) {
                Primitive primitive = Primitive.valueOf(BufUtils.readString(byteBuf));
                String key = BufUtils.readString(byteBuf);
                Object object = PrimitiveUtils.Buf.readByteBuf(byteBuf, primitive);
                primitiveMap.put(key, object);
            }
            return primitiveMap;
        }

        public static <Prim> Prim readByteBuf(ByteBuf byteBuf, Primitive primitive) {
            try {
                int len;
                switch (primitive) {
                    case BOOLEAN:
                        return (Prim) Boolean.valueOf(byteBuf.readBoolean());
                    case BOOLEAN_ARRAY:
                        len = BufUtils.readVarInt(byteBuf);
                        boolean[] bools = new boolean[len];
                        for (int i = 0; i < len; i++) {
                            bools[i] = byteBuf.readBoolean();
                        }
                        return (Prim) bools;
                    case INTEGER:
                        return (Prim) Integer.valueOf(BufUtils.readVarInt(byteBuf));
                    case INTEGER_ARRAY:
                        len = BufUtils.readVarInt(byteBuf);
                        int[] ints = new int[len];
                        for (int i = 0; i < len; i++) {
                            ints[i] = BufUtils.readVarInt(byteBuf);
                        }
                        return (Prim) ints;
                    case STRING:
                        return (Prim) BufUtils.readString(byteBuf);
                    case STRING_ARRAY:
                        len = BufUtils.readVarInt(byteBuf);
                        String[] strings = new String[len];
                        for (int i = 0; i < len; i++) {
                            strings[i] = BufUtils.readString(byteBuf);
                        }
                        return (Prim) strings;
                    case DOUBLE:
                        return (Prim) Double.valueOf(byteBuf.readDouble());
                    case DOUBLE_ARRAY:
                        len = BufUtils.readVarInt(byteBuf);
                        double[] doubles = new double[len];
                        for (int i = 0; i < len; i++) {
                            doubles[i] = byteBuf.readDouble();
                        }
                        return (Prim) doubles;
                    case SHORT:
                        return (Prim) Short.valueOf(byteBuf.readShort());
                    case SHORT_ARRAY:
                        len = BufUtils.readVarInt(byteBuf);
                        short[] shorts = new short[len];
                        for (int i = 0; i < len; i++) {
                            shorts[i] = byteBuf.readShort();
                        }
                        return (Prim) shorts;
                    case FLOAT:
                        return (Prim) Float.valueOf(byteBuf.readFloat());
                    case FLOAT_ARRAY:
                        len = BufUtils.readVarInt(byteBuf);
                        float[] floats = new float[len];
                        for (int i = 0; i < len; i++) {
                            floats[i] = byteBuf.readFloat();
                        }
                        return (Prim) floats;
                    case LONG:
                        return (Prim) Long.valueOf(BufUtils.readVarLong(byteBuf));
                    case LONG_ARRAY:
                        len = BufUtils.readVarInt(byteBuf);
                        long[] longs = new long[len];
                        for (int i = 0; i < len; i++) {
                            longs[i] = BufUtils.readVarLong(byteBuf);
                        }
                        return (Prim) longs;
                    case CHAR:
                        return (Prim) Character.valueOf(byteBuf.readChar());
                    case CHAR_ARRAY:
                        len = BufUtils.readVarInt(byteBuf);
                        char[] chars = new char[len];
                        for (int i = 0; i < len; i++) {
                            chars[i] = byteBuf.readChar();
                        }
                        return (Prim) chars;
                    case BYTE:
                        return (Prim) Byte.valueOf(byteBuf.readByte());
                    case BYTE_ARRAY:
                        len = BufUtils.readVarInt(byteBuf);
                        byte[] bytes = new byte[len];
                        for (int i = 0; i < len; i++) {
                            bytes[i] = byteBuf.readByte();
                        }
                        return (Prim) bytes;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        public static <Prim> void writeByteBuf(ByteBuf byteBuf, Prim object) {
            try {
                Primitive primitive = Primitive.of(object);
                int len;
                switch (primitive) {
                    case BOOLEAN:
                        boolean bool = (Boolean) object;
                        byteBuf.writeBoolean(bool);
                        break;
                    case BOOLEAN_ARRAY:
                        boolean[] bools = (boolean[]) object;
                        len = bools.length;
                        BufUtils.writeVarInt(len, byteBuf);
                        for (boolean b : bools) {
                            byteBuf.writeBoolean(b);
                        }
                        break;
                    case INTEGER:
                        int x = (Integer) object;
                        BufUtils.writeVarInt(x, byteBuf);
                        break;
                    case INTEGER_ARRAY:
                        int[] ints = (int[]) object;
                        len = ints.length;
                        BufUtils.writeVarInt(len, byteBuf);
                        for (int i : ints) {
                            BufUtils.writeVarInt(i, byteBuf);
                        }
                        break;
                    case STRING:
                        String string = (String) object;
                        BufUtils.writeString(string, byteBuf);
                        break;
                    case STRING_ARRAY:
                        String[] strings = (String[]) object;
                        len = strings.length;
                        BufUtils.writeVarInt(len, byteBuf);
                        for (String s : strings) {
                            BufUtils.writeString(s, byteBuf);
                        }
                        break;
                    case DOUBLE:
                        double dou = (Double) object;
                        byteBuf.writeDouble(dou);
                        break;
                    case DOUBLE_ARRAY:
                        double[] doubles = (double[]) object;
                        len = doubles.length;
                        BufUtils.writeVarInt(len, byteBuf);
                        for (double d : doubles) {
                            byteBuf.writeDouble(d);
                        }
                        break;
                    case SHORT:
                        short sho = (Short) object;
                        byteBuf.writeShort(sho);
                        break;
                    case SHORT_ARRAY:
                        short[] shorts = (short[]) object;
                        len = shorts.length;
                        BufUtils.writeVarInt(len, byteBuf);
                        for (short s : shorts) {
                            byteBuf.writeShort(s);
                        }
                        break;
                    case FLOAT:
                        float flo = (Float) object;
                        byteBuf.writeFloat(flo);
                        break;
                    case FLOAT_ARRAY:
                        float[] floats = (float[]) object;
                        len = floats.length;
                        BufUtils.writeVarInt(len, byteBuf);
                        for (float f : floats) {
                            byteBuf.writeFloat(f);
                        }
                        break;
                    case LONG:
                        long lo = (Long) object;
                        BufUtils.writeVarLong(lo, byteBuf);
                        break;
                    case LONG_ARRAY:
                        long[] longs = (long[]) object;
                        len = longs.length;
                        BufUtils.writeVarInt(len, byteBuf);
                        for (long l : longs) {
                            BufUtils.writeVarLong(l, byteBuf);
                        }
                        break;
                    case CHAR:
                        char ch = (Character) object;
                        byteBuf.writeChar(ch);
                        break;
                    case CHAR_ARRAY:
                        char[] chars = (char[]) object;
                        len = chars.length;
                        BufUtils.writeVarInt(len, byteBuf);
                        for (char c : chars) {
                            byteBuf.writeChar(c);
                        }
                        break;
                    case BYTE:
                        byte by = (Byte) object;
                        byteBuf.writeByte(by);
                        break;
                    case BYTE_ARRAY:
                        byte[] bytes = (byte[]) object;
                        len = bytes.length;
                        BufUtils.writeVarInt(len, byteBuf);
                        for (byte b : bytes) {
                            byteBuf.writeByte(b);
                        }
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
