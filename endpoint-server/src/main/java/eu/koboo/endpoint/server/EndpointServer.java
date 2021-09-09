package eu.koboo.endpoint.server;

import eu.koboo.endpoint.core.EndpointCore;
import eu.koboo.endpoint.core.builder.EndpointBuilder;
import eu.koboo.endpoint.core.codec.EndpointPacket;
import eu.koboo.endpoint.core.handler.EndpointInitializer;
import eu.koboo.endpoint.core.util.LocalThreadFactory;
import io.netty.bootstrap.ServerBootstrap;
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

  private final EndpointInitializer initializer;
  private final ServerBootstrap tcpBootstrap;
  private final ServerBootstrap udsBootstrap;
  private final ChannelGroup channelGroup;

  protected EndpointServer(EndpointBuilder endpointBuilder, int port) {
    super(endpointBuilder, port);

    // Get cores to calculate the event-loop-group sizes
    int bossSize = 2 * EndpointCore.CORES;
    int workerSize = 4 * EndpointCore.CORES;

    ChannelFactory<? extends ServerChannel> channelFactory = EndpointCore.createServerFactory();
    EventLoopGroup bossGroup = EndpointCore.createEventLoopGroup(bossSize, "ServerTCPBoss");
    EventLoopGroup workerGroup = EndpointCore.createEventLoopGroup(workerSize, "ServerTCPWorker");

    executorList.add(bossGroup);
    executorList.add(workerGroup);

    channelGroup = new DefaultChannelGroup("ServerChannelGroup",
        GlobalEventExecutor.INSTANCE);

    // Create the global ChannelInitializer
    initializer = new EndpointInitializer(this, channelGroup);

    // Create TCPBootstrap
    tcpBootstrap = new ServerBootstrap()
        .group(bossGroup, workerGroup)
        .channelFactory(channelFactory)
        .childHandler(initializer)
        .childOption(ChannelOption.TCP_NODELAY, true)
        .childOption(ChannelOption.SO_REUSEADDR, true)
        .childOption(ChannelOption.SO_KEEPALIVE, true);

    // Check for extra epoll-options
    if (Epoll.isAvailable()) {
      tcpBootstrap.childOption(EpollChannelOption.EPOLL_MODE, EpollMode.LEVEL_TRIGGERED);
    }

    udsBootstrap = new ServerBootstrap()
        .group(bossGroup, workerGroup)
        .channelFactory(EpollServerDomainSocketChannel::new)
        .childHandler(initializer);
  }

  /**
   * Let the server bind to the given port
   */
  @Override
  public boolean start() {

    if (getPort() == -1) {
      onException(getClass(), new RuntimeException("Connectivity error! port is not set!"));
      return false;
    }

    try {
      // Start the server and wait for socket to be bind to the given port
      SocketAddress address = new InetSocketAddress(getPort());
      tcpChannel = tcpBootstrap.bind(address).sync().channel();

      if(endpointBuilder.isUseUDS() && Epoll.isAvailable()) {
        DomainSocketAddress domainAddress = new DomainSocketAddress(EndpointCore.DEFAULT_UDS_PATH);
        udsChannel = udsBootstrap.bind(domainAddress).sync().channel();
      }

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
