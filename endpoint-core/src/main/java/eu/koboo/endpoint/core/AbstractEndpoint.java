package eu.koboo.endpoint.core;

import eu.koboo.endpoint.core.builder.EndpointBuilder;
import eu.koboo.endpoint.core.events.ConsumerEvent;
import eu.koboo.endpoint.core.events.EventHandler;
import eu.koboo.endpoint.core.events.endpoint.EndpointAction;
import eu.koboo.endpoint.core.events.endpoint.EndpointActionEvent;
import eu.koboo.endpoint.core.events.message.ErrorEvent;
import io.netty.channel.Channel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public abstract class AbstractEndpoint implements Endpoint {

  protected final EndpointBuilder endpointBuilder;
  protected final EventHandler eventBus;
  protected final EventExecutorGroup executorGroup;
  protected final List<EventExecutorGroup> executorList;
  protected Channel tcpChannel;
  protected Channel udsChannel;

  public AbstractEndpoint(EndpointBuilder builder) {
    endpointBuilder = builder;
    eventBus = new EventHandler(this);
    executorGroup = new DefaultEventExecutorGroup(EndpointCore.CORES * 2);
    executorList = new ArrayList<>();
    executorList.add(executorGroup);
  }

  @Override
  public boolean start() {
    eventBus.fireEvent(new EndpointActionEvent(this, EndpointAction.START));
    return true;
  }

  @Override
  public boolean stop() {
    try {
      boolean close = close();

      eventBus.fireEvent(new EndpointActionEvent(this, EndpointAction.STOP));

      for (EventExecutorGroup executorGroup : executorList) {
        executorGroup.shutdownGracefully();
      }

      return close;
    } catch (Exception e) {
      onException(getClass(), e);
    }
    return false;
  }

  @Override
  public boolean close() {
    try {
      eventBus.fireEvent(new EndpointActionEvent(this, EndpointAction.CLOSE));

      if (tcpChannel != null && tcpChannel.isOpen() && tcpChannel.isActive()) {
        tcpChannel.close().sync();
      }

      // Only try to close the udsChannel if we want to use udsChannel
      if (!isClient() && endpointBuilder.isUseUDS() && udsChannel != null && udsChannel.isOpen()
          && udsChannel.isActive()) {
        udsChannel.close().sync();
      }

      return true;
    } catch (InterruptedException e) {
      onException(getClass(), e);
    }
    return false;
  }

  @Override
  public EndpointBuilder builder() {
    return endpointBuilder;
  }

  @Override
  public boolean isConnected() {
    boolean isTCPConnected = tcpChannel != null && tcpChannel.isOpen() && tcpChannel.isActive();
    boolean isUDSConnected = udsChannel != null && udsChannel.isOpen()
        && udsChannel.isActive();

    // Only check the udsChannel if we want to use uds
    return !isClient() && endpointBuilder.isUseUDS() ? (isTCPConnected && isUDSConnected) : isTCPConnected;
  }

  @SuppressWarnings("all")
  @Override
  public Endpoint onException(Class clazz, Throwable error) {
    switch (endpointBuilder.getErrorMode()) {
      case EVENT:
        eventBus.fireEvent(new ErrorEvent(clazz, error));
        break;
      case STACK_TRACE:
        error.printStackTrace();
        break;
    }
    return this;
  }

  @Override
  public <T extends ConsumerEvent> Endpoint registerEvent(Class<T> eventClass,
      Consumer<? super T> consumer) {
    eventBus.register(eventClass, consumer);
    return this;
  }

  @Override
  public <T extends ConsumerEvent> Endpoint unregisterEvent(Class<T> eventClass,
      Consumer<T> consumer) {
    eventBus.unregister(eventClass, consumer);
    return this;
  }

  @Override
  public <T extends ConsumerEvent> CompletableFuture<T> fireEvent(T event) {
    return eventBus.fireEvent(event);
  }

  @Override
  public <T extends ConsumerEvent> boolean hasListener(Class<T> eventClass) {
    return eventBus.hasListener(eventClass);
  }

  public EventExecutorGroup executorGroup() {
    return executorGroup;
  }
}
