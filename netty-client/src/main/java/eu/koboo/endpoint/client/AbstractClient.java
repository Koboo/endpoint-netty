package eu.koboo.endpoint.client;

import eu.koboo.endpoint.core.AbstractEndpoint;
import eu.koboo.endpoint.core.builder.EndpointBuilder;

public abstract class AbstractClient extends AbstractEndpoint {

    private String host;
    private int port;

    /**
     * Default constructor of `AbstractEndpoint`
     */
    public AbstractClient(EndpointBuilder endpointBuilder, String host, int port) {
        super(endpointBuilder);
        this.host = host;
        this.port = port;
    }

    /**
     * @param object Send object to server
     * @param sync Handle `send`-call sync or async
     */
    public abstract void send(Object object, boolean sync);

    /**
     * @param object Send object to server
     */
    public void send(Object object) {
        send(object, false);
    }

    /**
     * @return Check if client is connected to server
     */
    public abstract boolean isConnected();

    /**
     * @return returns true, because client
     */
    @Override
    public boolean isClient() {
        return true;
    }

    /**
     * Sets the host and port of the client
     */
    public void setAddress(String host, int port) {
        if (host != null)
            this.host = host;
        if (port > 0)
            this.port = port;
    }

    /**
     * Getter for String host
     */
    public String getHost() {
        return host;
    }

    /**
     * Getter for int port
     */
    public int getPort() {
        return port;
    }
}
