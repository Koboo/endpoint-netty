package eu.koboo.endpoint.server;

import eu.koboo.endpoint.core.builder.EndpointBuilder;
import eu.koboo.endpoint.core.handler.EndpointInitializer;
import eu.koboo.nettyutils.LocalThreadFactory;
import eu.koboo.nettyutils.NettyType;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.net.InetSocketAddress;

public class EndpointServer extends AbstractServer {

    private final NettyType nettyType;
    private final ServerBootstrap serverBootstrap;
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private final ChannelGroup channels;
    private Channel channel;

    public EndpointServer(EndpointBuilder endpointBuilder, int port) {
        super(endpointBuilder, port);

        this.nettyType = NettyType.prepareType();


        // Get cores to calculate the event-loop-group sizes
        int cores = Runtime.getRuntime().availableProcessors();
        int bossSize = 2 * cores;
        int workerSize = 4 * cores;

        // Check and initialize the event-loop-groups
        this.bossGroup = nettyType.eventLoopGroup(bossSize, new LocalThreadFactory("EndpointServerBoss"));
        this.workerGroup = nettyType.eventLoopGroup(workerSize, new LocalThreadFactory("EndpointServerWorker"));

        this.channels = new DefaultChannelGroup("EndpointServerConnected", GlobalEventExecutor.INSTANCE);

        // Create ServerBootstrap
        this.serverBootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(nettyType.serverChannel())
                .childHandler(new EndpointInitializer(this, this.channels))
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.IP_TOS, 24)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        // Check for extra epoll-options
        if (nettyType.isEpoll()) {
            serverBootstrap
                    .childOption(EpollChannelOption.EPOLL_MODE, EpollMode.LEVEL_TRIGGERED)
                    .option(EpollChannelOption.TCP_FASTOPEN, 3)
                    .option(EpollChannelOption.SO_REUSEPORT, true);
        }
    }

    /**
     * Let the server bind to the given port
     */
    @Override
    public boolean start() {
        if (getPort() == -1) {
            onException(getClass(), new RuntimeException("port is not set!"));
            return false;
        }

        try {
            // Start the server and wait for socket to be bind to the given port
            this.channel = serverBootstrap.bind(new InetSocketAddress(getPort())).sync().channel();
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
     * @param sync
     */
    @Override
    public void send(Channel channel, Object object, boolean sync) {
        if (sync)
            try {
                channel.writeAndFlush(object).sync();
            } catch (InterruptedException e) {
                onException(getClass(), e);
            }
        else
            channel.writeAndFlush(object);
    }

    /**
     * Write the given object to the channel. This will be processed async
     *
     * @param object
     */
    public void send(Channel channel, Object object) {
        // use send-method, default-behaviour: async
        send(channel, object, false);
    }


    /**
     * Write the given object to all channels.
     *
     * @param object
     * @param sync
     */
    @Override
    public void sendAll(Object object, boolean sync) {
        for (Channel channel : channels) {
            send(channel, object, sync);
        }
    }

    /**
     * Write the given object to all channels. This will be processed async
     *
     * @param object
     */
    public void sendAll(Object object) {
        for (Channel channel : channels) {
            send(channel, object);
        }
    }

    /**
     * Get all channels connected to the server
     */
    @Override
    public ChannelGroup getChannelGroup() {
        return this.channels;
    }
}
