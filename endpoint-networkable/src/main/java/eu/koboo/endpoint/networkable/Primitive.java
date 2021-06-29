package eu.koboo.endpoint.networkable;

public enum Primitive {

    BOOLEAN(Boolean.class),
    BYTE(Byte.class),
    CHAR(Character.class),
    FLOAT(Float.class),
    DOUBLE(Double.class),
    SHORT(Short.class),
    LONG(Long.class),
    INTEGER(Integer.class),
    STRING(String.class);

    Class<?> primitiveClass;

    Primitive(Class<?> primitiveClass) {
        this.primitiveClass = primitiveClass;
    }

}
