package eu.koboo.endpoint.server;

import eu.koboo.endpoint.core.AbstractEndpoint;
import eu.koboo.endpoint.core.builder.EndpointBuilder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.group.ChannelGroup;

import java.util.Map;

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
    public abstract ChannelFuture send(Channel channel, Object object);

    /**
     * @param object Send object to client
     */
    public abstract void sendAndForget(Channel channel, Object object);

    /**
     * @param object Broadcast object
     */
    public abstract Map<Channel, ChannelFuture> broadcast(Object object);

    /**
     * @param object Broadcast object
     */
    public abstract void broadcastAndForget(Object object);

    /**
     * @return false, because we are the server
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
