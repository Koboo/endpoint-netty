package eu.koboo.endpoint.core.util;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BufUtils {

    public static byte[] toArray(ByteBuf wrap) {
        byte[] ret = new byte[wrap.readableBytes()];
        wrap.readBytes(ret);
        return ret;
    }

    public static ByteBuf writeString(String string, ByteBuf output) {
        byte[] values = string.getBytes(StandardCharsets.UTF_8);
        writeVarInt(values.length, output);
        output.writeBytes(values);
        return output;
    }

    public static String readString(ByteBuf input) {
        int integer = readVarInt(input);
        byte[] buffer = new byte[integer];
        input.readBytes(buffer, 0, integer);
        return new String(buffer, StandardCharsets.UTF_8);
    }

    public static String readStringCap(ByteBuf buf, int cap) {
        int length = readVarInt(buf);
        if (length < 0)
            throw new IllegalArgumentException("Got a negative-length string (" + length + ")");
        if (length == 0)
            throw new IllegalArgumentException("Got a zero-length string (" + length + ")");
        if (length > cap * 4)
            throw new IllegalArgumentException("Bad string size (got " + length + ", maximum is " + cap + ")");
        if (!buf.isReadable(length))
            throw new IllegalArgumentException("Trying to read a string that is too long (wanted " + length + ", only have " + buf.readableBytes() + ")");
        String str = buf.toString(buf.readerIndex(), length, StandardCharsets.UTF_8);
        buf.skipBytes(length);
        if (str.length() > cap)
            throw new IllegalArgumentException("Got a too-long string (got " + str.length() + ", max " + cap + ")");
        return str;
    }

    public static void writeStringCap(ByteBuf buffer, String string) {
        byte[] values = string.getBytes(CharsetUtil.UTF_8);
        if (values.length > 32767) {
            throw new IllegalArgumentException("String too big (was " + string.length() + " bytes encoded, max " + 32767 + ")");
        } else {
            writeVarInt(values.length, buffer);
            buffer.writeBytes(values);
        }
    }

    public static ByteBuf writeVarInt(int value, ByteBuf output) {
        do {
            byte temp = (byte) (value & 0b01111111);
            value >>>= 7;
            if (value != 0) {
                temp |= 0b10000000;
            }
            output.writeByte(temp);
        } while (value != 0);

        return output;
    }

    public static int readVarInt(ByteBuf input) {
        int numRead = 0;
        int result = 0;
        byte read;
        do {
            read = input.readByte();
            int value = (read & 0b01111111);
            result |= (value << (7 * numRead));

            numRead++;
            if (numRead > 5) {
                throw new RuntimeException("VarInt is too big");
            }
        } while ((read & 0b10000000) != 0);

        return result;
    }

    public static ByteBuf writeVarLong(long value, ByteBuf output) {
        do {
            byte temp = (byte) (value & 0b01111111);
            value >>>= 7;
            if (value != 0) {
                temp |= 0b10000000;
            }
            output.writeByte(temp);
        } while (value != 0);

        return output;
    }

    @SuppressWarnings("all")
    public static long readVarLong(ByteBuf input) {
        int numRead = 0;
        long result = 0;
        byte read;
        do {
            read = input.readByte();
            int value = (read & 0b01111111);
            result |= (value << (7 * numRead));

            numRead++;
            if (numRead > 10) {
                throw new RuntimeException("VarLong is too big");
            }
        } while ((read & 0b10000000) != 0);

        return result;
    }

    public static ByteBuf writeVarShort(int value, ByteBuf output) {
        int low = value & 0x7FFF;
        int high = (value & 0x7F8000) >> 15;
        if (high != 0) {
            low = low | 0x8000;
        }
        output.writeShort(low);
        if (high != 0) {
            output.writeByte(high);
        }
        return output;
    }

    public static int readVarShort(ByteBuf input) {
        int low = input.readUnsignedShort();
        int high = 0;
        if ((low & 0x8000) != 0) {
            low = low & 0x7FFF;
            high = input.readUnsignedByte();
        }
        return ((high & 0xFF) << 15) | low;
    }

    public static ByteBuf writeUUID(UUID value, ByteBuf output) {
        output.writeLong(value.getMostSignificantBits());
        output.writeLong(value.getLeastSignificantBits());
        return output;
    }

    public static UUID readUUID(ByteBuf input) {
        return new UUID(input.readLong(), input.readLong());
    }

    public static ByteBuf writeArray(byte[] value, ByteBuf output) {
        if (value.length > Short.MAX_VALUE) {
            throw new IndexOutOfBoundsException(String.format("Cannot send byte array longer than Short.MAX_VALUE (got %s bytes)", value.length));
        }
        writeVarInt(value.length, output);
        output.writeBytes(value);
        return output;
    }

    public static byte[] readArrayCap(ByteBuf buf, int cap) {
        int length = readVarInt(buf);
        if (length < 0)
            throw new IllegalArgumentException("Got a negative-length array (" + length + ")");
        if (length > cap)
            throw new IllegalArgumentException("Bad array size (got " + length + ", maximum is " + cap + ")");
        if (!buf.isReadable(length))
            throw new IllegalArgumentException("Trying to read an array that is too long (wanted " + length + ", only have " + buf.readableBytes() + ")");

        byte[] array = new byte[length];
        buf.readBytes(array);
        return array;
    }

    public static byte[] readArray(ByteBuf input) {
        int len = readVarInt(input);
        if (len > input.readableBytes()) {
            throw new IndexOutOfBoundsException(String.format("Cannot receive byte array longer than %s (got %s bytes)", input.readableBytes(), len));
        }
        byte[] ret = new byte[len];
        input.readBytes(ret);
        return ret;
    }

    public static ByteBuf writeStringArray(List<String> stringList, ByteBuf output) {
        writeVarInt(stringList.size(), output);
        for (String str : stringList) {
            writeString(str, output);
        }
        return output;
    }

    public static List<String> readStringArray(ByteBuf input) {
        int len = readVarInt(input);
        List<String> ret = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            ret.add(readString(input));
        }
        return ret;
    }
}