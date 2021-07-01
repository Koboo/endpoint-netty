package eu.koboo.endpoint.core.transfer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
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

    public <T> Optional<T> optional(String key, Class<T> clazz) {
        return Optional.ofNullable(get(key, clazz));
    }

    public TransferMap append(String key, Object object) {
        return this.put(key, object);
    }

    public TransferMap put(String key, Object value) {
        super.put(key, value);
        return this;
    }

    public static byte[] encode(TransferMap transferMap) {
        try {
            if(transferMap != null) {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                DataOutputStream outputStream = new DataOutputStream(buffer);
                int keyLength = transferMap.keySet().size();
                outputStream.writeInt(keyLength);
                for (Entry<String, Object> entry : transferMap.entrySet()) {
                    String key = entry.getKey();
                    Object object = entry.getValue();
                    Primitive primitive = Primitive.of(object);
                    if (primitive == null) {
                        throw new IllegalArgumentException("Value " + object.getClass().getName() + " is not a primitive type!");
                    }
                    outputStream.writeUTF(primitive.name());
                    outputStream.writeUTF(key);
                    Primitive.writeDynamic(outputStream, object);
                }
                buffer.close();
                return buffer.toByteArray();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    public static TransferMap decode(byte[] bytes) {
        TransferMap transferMap = new TransferMap();
        try {
            ByteArrayInputStream buffer = new ByteArrayInputStream(bytes);
            DataInputStream inputStream = new DataInputStream(buffer);
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
        return new TransferMap();
    }

}
