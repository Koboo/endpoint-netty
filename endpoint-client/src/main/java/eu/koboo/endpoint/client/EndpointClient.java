
package eu.koboo.endpoint.client;

import eu.koboo.endpoint.core.builder.EndpointBuilder;
import eu.koboo.endpoint.core.events.endpoint.EndpointEvent;
import eu.koboo.endpoint.core.handler.EndpointInitializer;
import eu.koboo.nettyutils.NettyType;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollMode;
import io.netty.channel.unix.DomainSocketAddress;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
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

        nettyType = NettyType.prepareType(endpointBuilder.isUsingUDS());

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

        if (endpointBuilder.isUsingUDS() && !nettyType.isUds() && getHost() == null && getPort() == -1) {
            onException(getClass(), new RuntimeException("Platform error! UnixDomainSocket is set, but no native transport available.."));
            return false;
        }

        if (!endpointBuilder.isUsingUDS() && (getHost() == null || getPort() == -1)) {
            onException(getClass(), new RuntimeException("Connectivity error! " + (getHost() == null ? "host-address is not set!" : "port is not set!")));
            return false;
        }

        // Close the Channel if it's already connected
        if (isConnected()) {
            onException(getClass(), new IllegalStateException("Connectivity error! Connection is already established!"));
            return false;
        }

        // Start the client and wait for the connection to be established.


        SocketAddress address = endpointBuilder.isUsingUDS() && nettyType.isUds() ?
                new DomainSocketAddress(endpointBuilder.getUDSFile()) :
                new InetSocketAddress(getHost(), getPort());

        ChannelFuture connectFuture = bootstrap.connect(address);

        ChannelFutureListener connectListener = future -> {
            if (!future.isSuccess()) {
                future.channel().close();
                start();
            } else {
                ChannelFutureListener closeListener = reconnectFuture -> scheduleReconnect(100);
                channel = future.channel();
                channel.closeFuture().addListener(closeListener);
            }
        };

        try {
            ChannelFuture future = connectFuture.addListener(connectListener).sync();
            if (!future.isSuccess())
                throw new IllegalStateException("Connectivity error! Connection is not established!");
            return super.start();
        } catch (InterruptedException e) {
            scheduleReconnect(1000);
        }

        return super.start();
    }

    private void scheduleReconnect(long millis) {
        group.schedule(() -> {
            if (!isConnected()) {
                eventHandler().handleEvent(new EndpointEvent(this, EndpointEvent.Action.RECONNECT));
                start();
            }
        }, millis, TimeUnit.MILLISECONDS);
    }

    @Override
    public NettyType nettyType() {
        return nettyType;
    }

}
