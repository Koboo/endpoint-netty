package eu.koboo.endpoint.core.events;

import io.netty.channel.Channel;

public class ReceiveEvent implements ConsumerEvent {

  private final Channel ctx;
  private final Object object;

  public ReceiveEvent(Channel ctx, Object object) {
    this.ctx = ctx;
    this.object = object;
  }

  public Channel getChannel() {
    return ctx;
  }

  public Object getObject() {
    return object;
  }

  @SuppressWarnings("unchecked")
  public <T> T getTypeObject() {
    return (T) object;
  }
}
