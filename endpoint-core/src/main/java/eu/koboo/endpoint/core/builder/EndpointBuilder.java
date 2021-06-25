package eu.koboo.endpoint.core.builder;

import eu.koboo.endpoint.core.builder.param.ErrorMode;
import eu.koboo.endpoint.core.util.Compression;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EndpointBuilder {

    /**
     * Method is deprecated. Use #builder() instead!
     */
    @Deprecated
    public static EndpointBuilder newBuilder() {
        return builder();
    }

    public static EndpointBuilder builder() {
        return new EndpointBuilder();
    }

    private Compression compression = Compression.NONE;
    private ErrorMode errorMode = ErrorMode.STACK_TRACE;

    private String udsFile = "/tmp/endpoint-netty/uds.sock";

    private boolean useTimeouts = false;
    private int writeTimeout = 15;
    private int readTimeout = 0;

    private boolean logging = false;

    private int autoReconnect = -1;

    private String encryption;

    private final Map<Integer, Class<?>> packetRegistry = new ConcurrentHashMap<>();

    private EndpointBuilder() {
    }

    public EndpointBuilder compression(Compression compression) {
        this.compression = compression;
        return this;
    }

    public EndpointBuilder errorMode(ErrorMode errorMode) {
        this.errorMode = errorMode;
        return this;
    }

    public EndpointBuilder useUDS(String socketFile) {
        this.udsFile = socketFile;
        return this;
    }

    public EndpointBuilder password(String password) {
        this.encryption = password;
        return this;
    }

    public EndpointBuilder timeout(int writeTimeout, int readTimeout) {
        this.useTimeouts = true;
        this.writeTimeout = writeTimeout;
        this.readTimeout = readTimeout;
        return this;
    }

    public EndpointBuilder logging(boolean logging) {
        this.logging = logging;
        return this;
    }

    public EndpointBuilder autoReconnect(int seconds) {
        this.autoReconnect = seconds;
        return this;
    }

    public EndpointBuilder disableReconnect() {
        this.autoReconnect = -1;
        return this;
    }

    public EndpointBuilder registerPacket(int id, Class<?> clazz) {
        if (packetRegistry.containsKey(id))
            throw new IllegalArgumentException("Id already used, please choose another.");
        packetRegistry.put(id, clazz);
        return this;
    }

    public Class<?> getClassById(int id) {
        return packetRegistry.getOrDefault(id, null);
    }

    public int getIdByClass(Class<?> clazz) {
        for (Map.Entry<Integer, Class<?>> entry : packetRegistry.entrySet())
            if (entry.getValue().getSimpleName().equalsIgnoreCase(clazz.getSimpleName()))
                return entry.getKey();

        return -1;
    }

    public Compression getCompression() {
        return compression;
    }

    public ErrorMode getErrorMode() {
        return errorMode;
    }

    public String getEncryption() {
        return encryption;
    }

    public boolean isUsingUDS() {
        return udsFile != null;
    }

    public String getUDSFile() {
        return udsFile;
    }

    public boolean isUsingTimeouts() {
        return useTimeouts;
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

    public int getAutoReconnect() {
        return autoReconnect;
    }
}
