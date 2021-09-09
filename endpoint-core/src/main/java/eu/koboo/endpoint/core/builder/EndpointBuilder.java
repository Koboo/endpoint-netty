package eu.koboo.endpoint.core.builder;

import eu.koboo.endpoint.core.builder.param.ErrorMode;
import eu.koboo.endpoint.core.codec.EndpointPacket;
import eu.koboo.endpoint.core.util.Compression;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class EndpointBuilder {

  private final Map<Integer, Supplier<? extends EndpointPacket>> supplierMap = new ConcurrentHashMap<>();
  private Compression compression = Compression.NONE;
  private ErrorMode errorMode = ErrorMode.STACK_TRACE;

  private int writeTimeout = 15;
  private int readTimeout = 0;

  private boolean logging = false;
  private boolean framing = true;
  private boolean processing = true;
  private boolean useUDS = true;

  private int autoReconnect = -1;

  private String encryptionPassword;

  private EndpointBuilder() {
  }

  public static EndpointBuilder builder() {
    return new EndpointBuilder();
  }

  public EndpointBuilder compression(Compression compression) {
    this.compression = compression;
    return this;
  }

  public EndpointBuilder errorMode(ErrorMode errorMode) {
    this.errorMode = errorMode;
    return this;
  }

  public EndpointBuilder useUDS(boolean value) {
    this.useUDS = value;
    return this;
  }

  public EndpointBuilder password(String password) {
    this.encryptionPassword = password;
    return this;
  }

  public EndpointBuilder timeout(int writeTimeout, int readTimeout) {
    this.writeTimeout = writeTimeout;
    this.readTimeout = readTimeout;
    return this;
  }

  public EndpointBuilder disableTimeouts() {
    this.writeTimeout = -1;
    this.readTimeout = -1;
    return this;
  }

  public EndpointBuilder logging(boolean logging) {
    this.logging = logging;
    return this;
  }

  public EndpointBuilder framing(boolean framing) {
    this.framing = framing;
    return this;
  }

  public EndpointBuilder processing(boolean processing) {
    this.processing = processing;
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

  public EndpointBuilder registerPacket(int id, Class<? extends EndpointPacket> clazz) {
    return registerPacket(id, () -> {
      try {
        return clazz.newInstance();
      } catch (InstantiationException | IllegalAccessException e) {
        e.printStackTrace();
      }
      return null;
    });
  }

  public EndpointBuilder registerPacket(int id, Supplier<? extends EndpointPacket> supplier) {
    if (supplierMap.containsKey(id)) {
      throw new IllegalArgumentException("Id '" + id + "' is already used.");
    }
    supplierMap.put(id, supplier);
    return this;
  }

  public <Type extends EndpointPacket> int getId(Type object) {
    for (Map.Entry<Integer, Supplier<? extends EndpointPacket>> entry : supplierMap.entrySet()) {
      if (entry.getValue().get().getClass().getName()
          .equalsIgnoreCase(object.getClass().getName())) {
        return entry.getKey();
      }
    }
    return Integer.MIN_VALUE;
  }

  @SuppressWarnings("unchecked")
  public <Type extends EndpointPacket> Supplier<Type> getSupplier(int id) {
    return (Supplier<Type>) supplierMap.get(id);
  }

  public Compression getCompression() {
    return compression;
  }

  public ErrorMode getErrorMode() {
    return errorMode;
  }

  public String getEncryptionPassword() {
    return encryptionPassword;
  }

  public boolean isUseUDS() {
    return useUDS;
  }

  public boolean isUsingTimeouts() {
    return writeTimeout > 0 || readTimeout > 0;
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

  public boolean isFraming() {
    return framing;
  }

  public boolean isProcessing() {
    return processing;
  }

  public int getAutoReconnect() {
    return autoReconnect;
  }
}
