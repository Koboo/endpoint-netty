
package eu.koboo.endpoint.client;

import eu.koboo.endpoint.core.builder.EndpointBuilder;
import eu.koboo.endpoint.core.events.endpoint.EndpointAction;
import eu.koboo.endpoint.core.events.endpoint.EndpointActionEvent;
import eu.koboo.endpoint.core.handler.EndpointInitializer;
import eu.koboo.endpoint.core.util.LocalThreadFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.channel.unix.DomainSocketAddress;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class EndpointClient extends AbstractClient {

    private final EventLoopGroup group;
    private final Bootstrap bootstrap;
    private Channel channel;

    public EndpointClient(EndpointBuilder endpointBuilder) {
        this(endpointBuilder, null, -1);
    }

    public EndpointClient(EndpointBuilder endpointBuilder, String host, int port) {
        super(endpointBuilder, host, port);

        // Get cores to calculate the event-loop-group sizes
        int cores = Runtime.getRuntime().availableProcessors();
        int workerSize = 4 * cores;

        // Check and initialize the event-loop-groups
        ThreadFactory localFactory = new LocalThreadFactory("EndpointClient");
        Class<? extends Channel> channelClass;
        if (Epoll.isAvailable()) {
            if (endpointBuilder.isUsingUDS()) {
                channelClass = EpollDomainSocketChannel.class;
            } else {
                channelClass = EpollSocketChannel.class;
            }
            group = new EpollEventLoopGroup(workerSize, localFactory);
        } else {
            channelClass = NioSocketChannel.class;
            group = new NioEventLoopGroup(workerSize, localFactory);
        }

        // Create Bootstrap
        bootstrap = new Bootstrap()
                .group(group)
                .channel(channelClass)
                .handler(new EndpointInitializer(this, null))
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

        // Check for extra epoll-options
        if (Epoll.isAvailable()) {
            bootstrap.option(EpollChannelOption.EPOLL_MODE, EpollMode.LEVEL_TRIGGERED);
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
        try {
            if (channel != null && channel.isActive())
                channel.close().sync();
            return super.close();
        } catch (InterruptedException e) {
            onException(getClass(), e);
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

            return close();
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

        if (endpointBuilder.isUsingUDS() && !Epoll.isAvailable() && getHost() == null && getPort() == -1) {
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
        SocketAddress address = endpointBuilder.isUsingUDS() && Epoll.isAvailable() ?
                new DomainSocketAddress(endpointBuilder.getUDSFile()) :
                new InetSocketAddress(getHost(), getPort());

        ChannelFuture connectFuture = bootstrap.connect(address);

        ChannelFutureListener connectListener = future -> {
            if (!future.isSuccess()) {
                if (future.channel() != null && future.channel().isActive())
                    future.channel().close();
                start();
            } else {
                ChannelFutureListener closeListener = reconnectFuture -> scheduleReconnect();
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
            scheduleReconnect();
        }

        return super.start();
    }


    /**
     * Write the given object to the server
     * and do something with the returned ChannelFuture.
     *
     * @param object
     */
    @Override
    public ChannelFuture send(Object object) {
        try {
            if (isConnected()) {
                return channel.writeAndFlush(object);
            }
        } catch (Exception e) {
            onException(getClass(), e);
        }
        return null;
    }

    /**
     * Write the given object to the server
     * and forget the send-future
     *
     * @param object
     */
    @Override
    public void sendAndForget(Object object) {
        send(object);
    }

    private void scheduleReconnect() {
        if (builder().getAutoReconnect() != -1) {
            long delay = TimeUnit.SECONDS.toMillis(builder().getAutoReconnect());
            group.schedule(() -> {
                if (!isConnected()) {
                    eventHandler().fireEvent(new EndpointActionEvent(this, EndpointAction.RECONNECT));
                    start();
                }
            }, delay, TimeUnit.MILLISECONDS);
        }
    }
}
