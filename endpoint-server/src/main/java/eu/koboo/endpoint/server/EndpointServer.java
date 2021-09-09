package eu.koboo.endpoint.server;

import eu.koboo.endpoint.core.builder.EndpointBuilder;
import eu.koboo.endpoint.core.codec.EndpointPacket;
import eu.koboo.endpoint.core.handler.EndpointInitializer;
import eu.koboo.endpoint.core.util.LocalThreadFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFactory;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollMode;
import io.netty.channel.epoll.EpollServerDomainSocketChannel;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.util.concurrent.GlobalEventExecutor;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ThreadFactory;

public class EndpointServer extends AbstractServer {

  private final ServerBootstrap serverBootstrap;
  private final ChannelGroup channelGroup;

  protected EndpointServer(EndpointBuilder endpointBuilder, int port) {
    super(endpointBuilder, port);

    // Get cores to calculate the event-loop-group sizes
    int bossSize = 2 * EndpointBuilder.CORES;
    int workerSize = 4 * EndpointBuilder.CORES;

    ThreadFactory bossFactory = new LocalThreadFactory("EndpointServerBoss");
    ThreadFactory workerFactory = new LocalThreadFactory("EndpointServerWorker");
    ChannelFactory<? extends ServerChannel> channelFactory;
    EventLoopGroup bossGroup;
    EventLoopGroup workerGroup;
    if (Epoll.isAvailable()) {
      if (endpointBuilder.isUsingUDS()) {
        channelFactory = EpollServerDomainSocketChannel::new;
      } else {
        channelFactory = EpollServerSocketChannel::new;
      }
      bossGroup = new EpollEventLoopGroup(bossSize, bossFactory);
      workerGroup = new EpollEventLoopGroup(workerSize, workerFactory);
    } else {
      channelFactory = NioServerSocketChannel::new;
      bossGroup = new NioEventLoopGroup(bossSize, bossFactory);
      workerGroup = new NioEventLoopGroup(workerSize, workerFactory);
    }

    executorList.add(bossGroup);
    executorList.add(workerGroup);

    channelGroup = new DefaultChannelGroup("EndpointServerChannelGroup",
        GlobalEventExecutor.INSTANCE);

    // Create ServerBootstrap
    serverBootstrap = new ServerBootstrap()
        .group(bossGroup, workerGroup)
        .channelFactory(channelFactory)
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
      onException(getClass(), new RuntimeException(
          "Platform error! UnixDomainSocket is set, but no native transport available.."));
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
   * Write the given packet to the channel.
   *
   * @param packet the packet, which get send to the channel
   */
  @Override
  public ChannelFuture send(Channel channel, EndpointPacket packet) {
    try {
      if (isConnected()) {
        return channel.writeAndFlush(packet);
      }
    } catch (Exception e) {
      onException(getClass(), e);
    }
    return null;
  }

  /**
   * Write the given packet to all channels.
   *
   * @param packet the packet, which get send to all connected channels
   */
  @Override
  public void broadcast(EndpointPacket packet) {
    for (Channel channel : channelGroup) {
      send(channel, packet);
    }
  }

  /**
   * Get all channels connected to the server
   */
  @Override
  public ChannelGroup getChannelGroup() {
    return channelGroup;
  }

}
