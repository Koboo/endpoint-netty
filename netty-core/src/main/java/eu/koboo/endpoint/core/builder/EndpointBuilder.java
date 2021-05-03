package eu.koboo.endpoint.core.builder;

import eu.binflux.serial.core.JavaSerialization;
import eu.binflux.serial.core.SerializerPool;
import eu.koboo.endpoint.core.builder.param.Protocol;
import eu.koboo.endpoint.core.builder.param.ErrorMode;
import eu.koboo.endpoint.core.builder.param.EventMode;
import eu.koboo.nettyutils.Compression;

public class EndpointBuilder {

    public static EndpointBuilder newBuilder() {
        return new EndpointBuilder();
    }

    private Protocol protocol;
    private Compression compression;
    private ErrorMode errorMode;
    private EventMode eventMode;

    private boolean fireIdleStates;
    private int writeTimeout;
    private int readTimeout;

    private boolean logging;
    private SerializerPool serializerPool;

    private EndpointBuilder() {
        this.protocol = Protocol.SERIALIZABLE;
        this.compression = Compression.GZIP;
        this.errorMode = ErrorMode.STACK_TRACE;
        this.eventMode = EventMode.EVENT_LOOP;

        this.fireIdleStates = false;
        this.writeTimeout = 15;
        this.readTimeout = 0;

        this.logging = false;
        this.serializerPool = new SerializerPool(JavaSerialization.class);
    }

    public EndpointBuilder protocol(Protocol protocol) {
        this.protocol = protocol;
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

    public Protocol getProtocol() {
        return protocol;
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
