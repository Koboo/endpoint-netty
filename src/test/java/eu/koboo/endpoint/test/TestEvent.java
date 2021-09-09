package eu.koboo.endpoint.test;

import eu.koboo.endpoint.core.events.ConsumerEvent;

public class TestEvent implements ConsumerEvent {

  private final String someString;
  private final int someInt;

  public TestEvent(String someString, int someInt) {
    this.someString = someString;
    this.someInt = someInt;
  }

  public String getSomeString() {
    return someString;
  }

  public int getSomeInt() {
    return someInt;
  }
}
