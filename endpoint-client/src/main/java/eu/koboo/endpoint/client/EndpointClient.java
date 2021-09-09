
package eu.koboo.endpoint.client;

import eu.koboo.endpoint.core.EndpointCore;
import eu.koboo.endpoint.core.builder.EndpointBuilder;
import eu.koboo.endpoint.core.codec.EndpointPacket;
import eu.koboo.endpoint.core.events.endpoint.EndpointAction;
import eu.koboo.endpoint.core.events.endpoint.EndpointActionEvent;
import eu.koboo.endpoint.core.events.message.LogEvent;
import eu.koboo.endpoint.core.handler.EndpointInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFactory;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollMode;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.util.concurrent.GlobalEventExecutor;
import java.io.File;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

public class EndpointClient extends AbstractClient {

  private final Bootstrap bootstrap;
  private final EndpointInitializer initializer;

  protected EndpointClient(EndpointBuilder endpointBuilder, String host, int port) {
    super(endpointBuilder, host, port);

    // Get cores to calculate the event-loop-group sizes
    int groupSize = 4 * EndpointCore.CORES;

    // Check and initialize the event-loop-groups
    ChannelFactory<? extends Channel> channelFactory = EndpointCore.createClientFactory();
    EventLoopGroup group = EndpointCore.createEventLoopGroup(groupSize, "ClientTCP");

    executorList.add(group);

    initializer = new EndpointInitializer(this, null);

    // Create TCPBootstrap
    bootstrap = new Bootstrap()
        .group(group)
        .channelFactory(channelFactory)
        .handler(initializer);

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

    if (getHost() == null || getPort() == -1) {
      onException(getClass(), new RuntimeException(
          "Connectivity error! " + (getHost() == null ? "host-address is not set!"
              : "port is not set!")));
      return false;
    }

    // Close the Channel if it's already connected
    if (isConnected()) {
      onException(getClass(),
          new IllegalStateException("Connectivity error! Connection is already established!"));
      return false;
    }

    // Start the client and wait for the connection to be established.
    SocketAddress address = new InetSocketAddress(getHost(), getPort());
    if(Epoll.isAvailable() && getHost().equalsIgnoreCase("localhost")) {
      File udsFile = new File(EndpointCore.DEFAULT_UDS_PATH);
      if(udsFile.exists()) {
        address = new DomainSocketAddress(EndpointCore.DEFAULT_UDS_PATH);
        fireEvent(new LogEvent("Found localhost! Using client-side unix-domain-socket!"));
      }
    }

    ChannelFuture connectFuture = bootstrap.connect(address);

    ChannelFutureListener connectListener = future -> {
      if (!future.isSuccess()) {
          if (future.channel() != null && future.channel().isActive()) {
              future.channel().close();
          }
        start();
      } else {
        ChannelFutureListener closeListener = reconnectFuture -> scheduleReconnect();
        tcpChannel = future.channel();
        tcpChannel.closeFuture().addListener(closeListener);
      }
    };

    try {
      ChannelFuture future = connectFuture.addListener(connectListener).sync();
        if (!future.isSuccess()) {
            throw new IllegalStateException("Connectivity error! Connection is not established!");
        }
      return super.start();
    } catch (InterruptedException e) {
      scheduleReconnect();
    }

    return super.start();
  }


  /**
   * Write the given object to the server and do something with the returned ChannelFuture.
   *
   * @param packet the packet, which get send to the server
   */
  @Override
  public ChannelFuture send(EndpointPacket packet) {
    try {
      if (isConnected()) {
        return tcpChannel.writeAndFlush(packet);
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
