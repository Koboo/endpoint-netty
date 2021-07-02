package eu.koboo.endpoint.core.primitive;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unchecked")
public class PrimitiveMap extends ConcurrentHashMap<String, Object> {

    public PrimitiveMap() {
    }

    public <T> T get(String key) {
        return (T) super.get(key);
    }

    public <T> T get(String key, Class<T> typeClass) {
        return (T) super.get(key);
    }

    public <T> T getOrDefault(String key, T defaultType) {
        return (T) super.getOrDefault(key, defaultType);
    }

    public <T> Optional<T> optional(String key) {
        return Optional.ofNullable(get(key));
    }

    public <T> Optional<T> optional(String key, Class<T> typeClass) {
        return Optional.ofNullable(get(key, typeClass));
    }

    public PrimitiveMap append(String key, Object object) {
        try {
            super.put(key, object);
        } catch (Exception ignored) { }
        return this;
    }

    public PrimitiveMap put(String key, Object value) {
        super.put(key, value);
        return this;
    }
}
