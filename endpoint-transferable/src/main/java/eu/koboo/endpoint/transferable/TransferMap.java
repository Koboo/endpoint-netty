package eu.koboo.endpoint.transferable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unchecked")
public class TransferMap extends ConcurrentHashMap<String, Object> {

    public TransferMap() {
    }

    public <T> T get(String key) {
        return (T) super.get(key);
    }

    public <T> T get(String key, Class<T> typeClass) {
        return (T) super.get(key);
    }

    public <T> T getOrDefault(String key, T defaultT) {
        return (T) super.getOrDefault(key, defaultT);
    }

    public <T> Optional<T> optional(String key) {
        return Optional.ofNullable(get(key));
    }

    public TransferMap append(String key, Object object) {
        return this.put(key, object);
    }

    public TransferMap put(String key, Object value) {
        if(!Primitive.isPrimitive(value)) {
            throw new IllegalArgumentException("Value " + value.getClass().getName() + " is not a primitive type!");
        }
        super.put(key, value);
        return this;
    }

    public static byte[] encode(TransferMap transferMap) {
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            DataOutputStream outputStream = new DataOutputStream(buffer);
            int keyLength = transferMap.keySet().size();
            outputStream.writeInt(keyLength);
            for(Map.Entry<String, Object> entry : transferMap.entrySet()) {
                String key = entry.getKey();
                Object object = entry.getValue();
                Primitive primitive = Primitive.of(object);
                if(primitive == null) {
                    throw new IllegalArgumentException("Value " + object.getClass().getName() + " is not a primitive type!");
                }
                outputStream.writeUTF(primitive.name());
                outputStream.writeUTF(key);
                Primitive.writeDynamic(outputStream, object);
            }
            buffer.close();
            return buffer.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static TransferMap decode(byte[] bytes) {
        try {
            ByteArrayInputStream buffer = new ByteArrayInputStream(bytes);
            DataInputStream inputStream = new DataInputStream(buffer);
            TransferMap transferMap = new TransferMap();
            int keyLength = inputStream.readInt();
            for(int i = 0; i < keyLength; i++) {
                Primitive primitive = Primitive.valueOf(inputStream.readUTF());
                String key = inputStream.readUTF();
                Object object = Primitive.readDynamic(inputStream, primitive);
                transferMap.put(key, object);
            }
            return transferMap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
