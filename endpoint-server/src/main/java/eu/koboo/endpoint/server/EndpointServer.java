package eu.koboo.endpoint.server;

import eu.koboo.endpoint.core.builder.EndpointBuilder;
import eu.koboo.endpoint.core.handler.EndpointInitializer;
import eu.koboo.endpoint.core.util.LocalThreadFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;

public class EndpointServer extends AbstractServer {

    private final ServerBootstrap serverBootstrap;
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private final ChannelGroup channelGroup;
    private Channel channel;

    public EndpointServer(EndpointBuilder endpointBuilder) {
        this(endpointBuilder, -1);
    }

    public EndpointServer(EndpointBuilder endpointBuilder, int port) {
        super(endpointBuilder, port);

        // Get cores to calculate the event-loop-group sizes
        int cores = Runtime.getRuntime().availableProcessors();
        int bossSize = 2 * cores;
        int workerSize = 4 * cores;

        ThreadFactory bossFactory = new LocalThreadFactory("EndpointServerBoss");
        ThreadFactory workerFactory = new LocalThreadFactory("EndpointServerWorker");
        Class<? extends ServerChannel> channelClass;
        if (Epoll.isAvailable()) {
            if (endpointBuilder.isUsingUDS()) {
                channelClass = EpollServerDomainSocketChannel.class;
            } else {
                channelClass = EpollServerSocketChannel.class;
            }
            bossGroup = new EpollEventLoopGroup(bossSize, bossFactory);
            workerGroup = new EpollEventLoopGroup(workerSize, workerFactory);
        } else {
            channelClass = NioServerSocketChannel.class;
            bossGroup = new NioEventLoopGroup(bossSize, bossFactory);
            workerGroup = new NioEventLoopGroup(workerSize, workerFactory);
        }

        channelGroup = new DefaultChannelGroup("EndpointServerConnected", GlobalEventExecutor.INSTANCE);

        // Create ServerBootstrap
        serverBootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(channelClass)
                .childHandler(new EndpointInitializer(this, channelGroup))
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

        if (!endpointBuilder.isUsingUDS()) {
            serverBootstrap
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_REUSEADDR, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
        }

        // Check for extra epoll-options
        if (Epoll.isAvailable()) {
            serverBootstrap.childOption(EpollChannelOption.EPOLL_MODE, EpollMode.LEVEL_TRIGGERED);
        }
    }

    /**
     * Let the server bind to the given port
     */
    @Override
    public boolean start() {


        if (endpointBuilder.isUsingUDS() && !Epoll.isAvailable() && getPort() == -1) {
            onException(getClass(), new RuntimeException("Platform error! UnixDomainSocket is set, but no native transport available.."));
            return false;
        }

        if (!endpointBuilder.isUsingUDS() && getPort() == -1) {
            onException(getClass(), new RuntimeException("Connectivity error! port is not set!"));
            return false;
        }

        try {
            SocketAddress address = endpointBuilder.isUsingUDS() && Epoll.isAvailable() ?
                    new DomainSocketAddress(endpointBuilder.getUDSFile()) :
                    new InetSocketAddress(getPort());

            // Start the server and wait for socket to be bind to the given port
            channel = serverBootstrap.bind(address).sync().channel();
            return super.start();
        } catch (InterruptedException e) {
            onException(getClass(), e);
        }
        return false;
    }

    /**
     * Stops the server socket.
     */
    @Override
    public boolean stop() {
        try {

            // shutdown eventloop-groups
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();

            // close server-channel
            channel.close().sync();
            return super.stop();
        } catch (Exception e) {
            onException(getClass(), e);
        }
        return false;
    }

    /**
     * Closes the server socket.
     */
    @Override
    public boolean close() {
        if (channel != null && channel.isOpen())
            try {
                channel.close().sync();
                return super.close();
            } catch (InterruptedException e) {
                onException(getClass(), e);
            }
        return false;
    }


    /**
     * Write the given object to the channel.
     *
     * @param object
     */
    @Override
    public ChannelFuture send(Channel channel, Object object) {
        return channel.writeAndFlush(object);
    }

    /**
     * Write the given object to the channel.
     *
     * @param object
     */
    public void sendAndForget(Channel channel, Object object) {
        send(channel, object);
    }

    /**
     * Write the given object to all channels.
     *
     * @param object
     */
    @Override
    public Map<Channel, ChannelFuture> broadcast(Object object) {
        Map<Channel, ChannelFuture> channelFutureMap = new ConcurrentHashMap<>();
        for (Channel channel : channelGroup) {
            ChannelFuture future = send(channel, object);
            channelFutureMap.put(channel, future);
        }
        return channelFutureMap;
    }

    /**
     * Write the given object to all channels.
     *
     * @param object
     */
    @Override
    public void broadcastAndForget(Object object) {
        broadcast(object).clear();
    }

    /**
     * Get all channels connected to the server
     */
    @Override
    public ChannelGroup getChannelGroup() {
        return this.channelGroup;
    }

}
