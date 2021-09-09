package eu.koboo.endpoint.server;

import eu.koboo.endpoint.core.AbstractEndpoint;
import eu.koboo.endpoint.core.builder.EndpointBuilder;
import eu.koboo.endpoint.core.codec.EndpointPacket;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
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

    public abstract ChannelFuture send(Channel channel, EndpointPacket packet);

    public abstract void broadcast(EndpointPacket packet);

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
