package eu.koboo.endpoint.core.builder;

import eu.binflux.serial.core.JavaSerialization;
import eu.binflux.serial.core.SerializerPool;
import eu.koboo.endpoint.core.builder.param.ErrorMode;
import eu.koboo.endpoint.core.builder.param.EventMode;
import eu.koboo.endpoint.core.codec.AbstractEndpointCodec;
import eu.koboo.nettyutils.Compression;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EndpointBuilder {

    public static EndpointBuilder newBuilder() {
        return new EndpointBuilder();
    }

    private Class<? extends AbstractEndpointCodec<?>> endpointCodecClass;

    private Compression compression = Compression.NONE;
    private ErrorMode errorMode = ErrorMode.STACK_TRACE;
    private EventMode eventMode = EventMode.EVENT_LOOP;

    private String udsFile = "/tmp/endpoint-netty/uds.sock";

    private boolean fireIdleStates = false;
    private int writeTimeout = 15;
    private int readTimeout = 0;

    private boolean logging = false;
    private SerializerPool serializerPool = new SerializerPool(JavaSerialization.class);

    private final Map<Integer, Class<?>> packetRegistry = new ConcurrentHashMap<>();

    private EndpointBuilder() {
    }

    public EndpointBuilder codec(Class<? extends AbstractEndpointCodec<?>> endpointCodecClass) {
        this.endpointCodecClass = endpointCodecClass;
        return this;
    }

    public EndpointBuilder compression(Compression compression) {
        this.compression = compression;
        return this;
    }

    public EndpointBuilder errorMode(ErrorMode errorMode) {
        this.errorMode = errorMode;
        return this;
    }

    public EndpointBuilder eventMode(EventMode eventMode) {
        this.eventMode = eventMode;
        return this;
    }

    public EndpointBuilder isUsingUDS(String socketFile) {
        this.udsFile = socketFile;
        return this;
    }

    public EndpointBuilder idleState(int writeTimeout, int readTimeout) {
        this.fireIdleStates = true;
        this.writeTimeout = writeTimeout;
        this.readTimeout = readTimeout;
        return this;
    }

    public EndpointBuilder logging(boolean logging) {
        this.logging = logging;
        return this;
    }

    public EndpointBuilder serializer(SerializerPool serializerPool) {
        this.serializerPool = serializerPool;
        return this;
    }

    public EndpointBuilder registerPacket(int oid, Class<?> clazz) {
        if(packetRegistry.containsKey(oid))
            throw new IllegalArgumentException("Id already used, please choose another.");
        packetRegistry.put(oid, clazz);
        return this;
    }

    public Class<?> getClassById(int oid) {
        return packetRegistry.getOrDefault(oid, null);
    }

    public int getIdByClass(Class<?> clazz) {
        for(Map.Entry<Integer, Class<?>> entry : packetRegistry.entrySet()) {
            if(entry.getValue().getSimpleName().equalsIgnoreCase(clazz.getSimpleName())) {
                return entry.getKey();
            }
        }
        return -1;
    }

    public Class<? extends AbstractEndpointCodec<?>> getEndpointCodecClass() {
        return endpointCodecClass;
    }

    public Compression getCompression() {
        return compression;
    }

    public ErrorMode getErrorMode() {
        return errorMode;
    }

    public EventMode getEventMode() {
        return eventMode;
    }

    public boolean isUsingUDS() {
        return udsFile != null;
    }

    public String getUDSFile() {
        return udsFile;
    }

    public boolean isFireIdleStates() {
        return fireIdleStates;
    }

    public int getWriteTimeout() {
        return writeTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public boolean isLogging() {
        return logging;
    }

    public SerializerPool getSerializerPool() {
        return serializerPool;
    }
}
