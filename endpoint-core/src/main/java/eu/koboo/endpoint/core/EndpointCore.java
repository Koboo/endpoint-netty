package eu.koboo.endpoint.core;

import eu.koboo.endpoint.core.util.LocalThreadFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFactory;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.io.File;
import java.util.concurrent.ThreadFactory;

public class EndpointCore {

  public static final int CORES = Runtime.getRuntime().availableProcessors();
  public static final String DEFAULT_UDS_PATH = "tmp/endpoint-netty-uds.sock";

  static {
    File file = new File(DEFAULT_UDS_PATH);
    File parent = new File(file.getAbsolutePath().replaceFirst(file.getName(), ""));
    if (parent.isDirectory() && !parent.exists()) {
      parent.mkdirs();
    }
  }

  public static ChannelFactory<? extends ServerChannel> createServerFactory() {
    if (Epoll.isAvailable()) {
      return EpollServerSocketChannel::new;
    } else {
      return NioServerSocketChannel::new;
    }
  }

  public static ChannelFactory<? extends Channel> createClientFactory() {
    if (Epoll.isAvailable()) {
      return EpollSocketChannel::new;
    } else {
      return NioSocketChannel::new;
    }
  }

  public static EventLoopGroup createEventLoopGroup(int size, String name) {
    ThreadFactory bossFactory = new LocalThreadFactory(name);
    EventLoopGroup eventLoopGroup;
    if (Epoll.isAvailable()) {
      eventLoopGroup = new EpollEventLoopGroup(size, bossFactory);
    } else {
      eventLoopGroup = new NioEventLoopGroup(size, bossFactory);
    }
    return eventLoopGroup;
  }

}
