package eu.koboo.endpoint.server;

import eu.koboo.endpoint.core.AbstractEndpoint;
import eu.koboo.endpoint.core.builder.EndpointBuilder;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;

public abstract class AbstractServer extends AbstractEndpoint {

    private int port;

    /**
     * Default constructor of `AbstractEndpoint`
     */
    public AbstractServer(EndpointBuilder endpointBuilder, int port) {
        super(endpointBuilder);
        this.port = port;
    }

    public abstract ChannelGroup getChannelGroup();

    /**
     * @param object Send object to client
     */
    public abstract void send(Channel channel, Object object, boolean sync);

    /**
     * @param object Send object to all clients
     */
    public abstract void sendAll(Object object, boolean sync);

    /**
     * @return returns false, because server
     */
    @Override
    public boolean isClient() {
        return false;
    }

    /**
     * Sets the port for the server
     */
    public void setPort(int port) {
        if (port > 0)
            this.port = port;
    }
    /**
     * Getter of the port
     */
    public int getPort() {
        return port;
    }

}
