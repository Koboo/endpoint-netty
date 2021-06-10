package eu.koboo.endpoint.client;

import eu.koboo.endpoint.core.AbstractEndpoint;
import eu.koboo.endpoint.core.builder.EndpointBuilder;
import io.netty.channel.ChannelFuture;

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
     * @return Check if client is connected to server
     */
    public abstract boolean isConnected();

    /**
     * @return true, because we are the client
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

    /**
     * @param object Send object to server
     */
    public abstract ChannelFuture send(Object object);

    /**
     * @param object Send object to server
     */
    public abstract void sendAndForget(Object object);

}
