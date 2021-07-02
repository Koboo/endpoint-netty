package eu.koboo.endpoint.core.transfer;

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
}
