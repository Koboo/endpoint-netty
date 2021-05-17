
package eu.koboo.endpoint.client;

import eu.koboo.endpoint.core.builder.EndpointBuilder;
import eu.koboo.endpoint.core.handler.EndpointInitializer;
import eu.koboo.nettyutils.NettyType;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.*;
import io.netty.channel.unix.DomainSocketAddress;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

public class EndpointClient extends AbstractClient {

    private final NettyType nettyType;
    private final EventLoopGroup group;
    private final Bootstrap bootstrap;
    private Channel channel;

    public EndpointClient(EndpointBuilder endpointBuilder) {
        this(endpointBuilder, null, -1);
    }

    public EndpointClient(EndpointBuilder endpointBuilder, String host, int port) {
        super(endpointBuilder, host, port);

        nettyType = NettyType.prepareType(endpointBuilder.getDomainSocket() != null);

        // Get cores to calculate the event-loop-group sizes
        int cores = Runtime.getRuntime().availableProcessors();
        int workerSize = 4 * cores;

        // Check and initialize the event-loop-groups
        group = nettyType.eventLoopGroup(workerSize, "EndpointClient");

        // Create Bootstrap
        bootstrap = new Bootstrap()
                .group(group)
                .channel(nettyType.clientClass())
                .handler(new EndpointInitializer(this, null))
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

        // Check for extra epoll-options
        if (nettyType.isEpoll() && !nettyType.isUds()) {
            bootstrap
                    .option(EpollChannelOption.EPOLL_MODE, EpollMode.LEVEL_TRIGGERED)
                    .option(ChannelOption.TCP_FASTOPEN_CONNECT, true);
        }
    }

    /**
     * Write the given object to the channel.
     *
     * @param object
     * @param sync
     */
    @Override
    public void send(Object object, boolean sync) {
        try {
            if (isConnected()) {
                if (sync) {
                    channel.writeAndFlush(object).sync();
                } else {
                    executor().execute(() -> channel.writeAndFlush(object));
                }
            }
        } catch (Exception e) {
            onException(getClass(), e);
        }
    }

    /**
     * Return if the client is connected or not
     */
    @Override
    public boolean isConnected() {
        return channel != null && channel.isOpen() && channel.isActive();
    }

    /**
     * Close only the channel
     */
    @Override
    public boolean close() {
        if (isConnected()) {
            try {
                channel.close().sync();
                return super.close();
            } catch (InterruptedException e) {
                onException(getClass(), e);
            }
        }
        return false;
    }

    /**
     * Close the endpoint
     */
    @Override
    public boolean stop() {
        try {

            group.shutdownGracefully();

            close();
            return super.close();
        } catch (Exception e) {
            onException(getClass(), e);
        }
        return false;
    }

    /**
     * Connects the client to the given host and port
     */
    @Override
    public boolean start() {

        if((getHost() == null || getPort() == -1) && !nettyType.isUds() && endpointBuilder.getDomainSocket() != null) {
            onException(getClass(), new RuntimeException("Platform error! DomainSocket is set, but no native transport available.."));
        }

        // Check if host and port is set
        if ((getHost() == null || getPort() == -1) && !nettyType.isUds()) {
            onException(getClass(), new RuntimeException("Connectivity error! " + (getHost() == null ? "host-address is not set!" : "port is not set!")));
            return false;
        }

        // Close the Channel if it's already connected
        if (isConnected()) {
            onException(getClass(), new IllegalStateException("Connectivity error! Connection is already established!"));
            return false;
        }

        // Start the client and wait for the connection to be established.
        try {
            if(nettyType.isUds()) {
                channel = bootstrap.connect(new DomainSocketAddress(endpointBuilder.getDomainSocket())).sync().channel();
            } else {
                channel = bootstrap.connect(new InetSocketAddress(getHost(), getPort())).sync().channel();
            }
            channel.closeFuture().addListener((ChannelFuture future) -> future.channel().eventLoop().schedule((Runnable) this::start, 5, TimeUnit.SECONDS));
            return super.start();
        } catch (InterruptedException e) {
            onException(getClass(), e);
        }
        return false;
    }

}
