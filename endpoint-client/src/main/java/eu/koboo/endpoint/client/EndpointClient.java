
package eu.koboo.endpoint.client;

import eu.koboo.endpoint.core.builder.EndpointBuilder;
import eu.koboo.endpoint.core.codec.EndpointPacket;
import eu.koboo.endpoint.core.events.endpoint.EndpointAction;
import eu.koboo.endpoint.core.events.endpoint.EndpointActionEvent;
import eu.koboo.endpoint.core.handler.EndpointInitializer;
import eu.koboo.endpoint.core.primitive.PrimitiveMap;
import eu.koboo.endpoint.core.primitive.PrimitivePacket;
import eu.koboo.endpoint.core.util.LocalThreadFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class EndpointClient extends AbstractClient {

    private final Bootstrap bootstrap;

    protected EndpointClient(EndpointBuilder endpointBuilder, String host, int port) {
        super(endpointBuilder, host, port);

        // Get cores to calculate the event-loop-group sizes
        int workerSize = 4 * EndpointBuilder.CORES;

        // Check and initialize the event-loop-groups
        ThreadFactory localFactory = new LocalThreadFactory("EndpointClient");
        ChannelFactory<? extends Channel> channelFactory;
        EventLoopGroup group;
        if (Epoll.isAvailable()) {
            if (endpointBuilder.isUsingUDS()) {
                channelFactory = EpollDomainSocketChannel::new;
            } else {
                channelFactory = EpollSocketChannel::new;
            }
            group = new EpollEventLoopGroup(workerSize, localFactory);
        } else {
            channelFactory = NioSocketChannel::new;
            group = new NioEventLoopGroup(workerSize, localFactory);
        }

        executorList.add(group);

        // Create Bootstrap
        bootstrap = new Bootstrap()
                .group(group)
                .channelFactory(channelFactory)
                .handler(new EndpointInitializer(this, null))
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

        // Check for extra epoll-options
        if (Epoll.isAvailable()) {
            bootstrap.option(EpollChannelOption.EPOLL_MODE, EpollMode.LEVEL_TRIGGERED);
        }
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
     * @param object the packet, which get send to the server
     */
    @Override
    public ChannelFuture send(Object object) {
        try {
            EndpointPacket packet;
            if(!(object instanceof EndpointPacket) && !(object instanceof PrimitiveMap)) {
                throw new IllegalArgumentException("Object '" + object.getClass().getName() + "' doesn't implement " + EndpointPacket.class.getSimpleName() + " or " + PrimitiveMap.class.getSimpleName());
            }
            if(object instanceof PrimitiveMap) {
                packet = new PrimitivePacket().setPrimitiveMap((PrimitiveMap) object);
            } else {
                packet = (EndpointPacket) object;
            }
            if (isConnected()) {
                return channel.writeAndFlush(packet);
            }
        } catch (Exception e) {
            onException(getClass(), e);
        }
        return null;
    }

    private void scheduleReconnect() {
        if (builder().getAutoReconnect() != -1) {
            long delay = TimeUnit.SECONDS.toMillis(builder().getAutoReconnect());
            GlobalEventExecutor.INSTANCE.schedule(() -> {
                if (!isConnected()) {
                    fireEvent(new EndpointActionEvent(this, EndpointAction.RECONNECT));
                    start();
                }
            }, delay, TimeUnit.MILLISECONDS);
        }
    }
}
