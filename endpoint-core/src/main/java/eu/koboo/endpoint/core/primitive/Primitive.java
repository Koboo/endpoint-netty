package eu.koboo.endpoint.core.primitive;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
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
    UUID(UUID.class, UUID.class),

    BOOLEAN_ARRAY(Boolean[].class, boolean[].class),
    BYTE_ARRAY(Byte[].class, byte[].class),
    CHAR_ARRAY(Character[].class, char[].class),
    FLOAT_ARRAY(Float[].class, float[].class),
    DOUBLE_ARRAY(Double[].class, double[].class),
    SHORT_ARRAY(Short[].class, short[].class),
    LONG_ARRAY(Long[].class, long[].class),
    INTEGER_ARRAY(Integer[].class, int[].class),
    STRING_ARRAY(String[].class, String[].class),
    UUID_ARRAY(UUID[].class, UUID[].class),
    ;


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

}