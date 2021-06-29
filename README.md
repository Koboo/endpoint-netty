![Binflux-Netty](binflux-netty.png)

Endpoint-Netty offers a fast, simple and secure way to define 
your own protocol and process it accordingly. 
The main functions are the definition of the protocol, 
the encoding of the packets and the provision of a consumer-based event bus.

Further information can be found in the following documentation.

## History 

The [initial project](https://github.com/EsotericSoftware/kryonetty) was
developed by [EsotericSoftware](https://github.com/EsotericSoftware).
Since [KryoNetty](https://github.com/EsotericSoftware/kryonetty) did not work with [Netty 4](https://netty.io), 
I ported to it and created a [fork](https://github.com/BinfluxDev/binflux-netty) to make my own customizations.
During the porting I already made some changes to achieve my set requirements. Since I put a big 
focus on performance in the later development, the serialization by [Kryo](https://github.com/EsotericSoftware/kryo) was removed.

I've decided to release a "customized version" and now this project is
called **EndpointNetty**. The biggest difference between **EndpointNetty** and **BinfluxNetty** is: [Manual Packet-Encoding](#how-to-create-packets) instead of [Automatic serialization by Kryo](https://github.com/EsotericSoftware/kryo).

## Overview

#### Endpoints
  * [EndpointBuilder](#what-is-the-endpointbuilder)
  * [Options](#options)
  * [Timeouts](#timeout-options)
  * [Building Endpoints](#how-to-build-the-endpoints)
  * [Starting Endpoints](#how-to-start-the-endpoints)
  * [Reconnecting with Client](#reconnecting-with-endpointclient)
#### Packets
  * [Creating Packets](#how-to-create-packets)
  * [Sending Packets](#how-to-send-packets)
#### Events
  * [Default Events](#default-events)
  * [Registering Events](#register-events)
  * [Unregistering Events](#unregister-events)
  * [Creating new Events](#create-new-events)  
#### Networkable
  * [What is a Networkables](#what-is-a-networkable)
  * [Creating Networkable](#how-to-create-networkables)
  * [En-/Decoding Networkable](#how-to-encode-or-decode-networkable)
#### Build and Download
  * [Download](#add-as-dependency)  
  * [Build From Source](#build-from-source)


## What is the EndpointBuilder
`EndpointBuilder` passes options to endpoints. To create a new instance:

```java
EndpointBuilder builder = EndpointBuilder.builder()
        // Add more options by fluent calls
        .logging(false)
        .framing(true)
        .processing(true)
        .timeout(15, 0);
```

#### Options:
* `logging(boolean value)` 
    * enables/disables built-in `LoggingHandler.class` of netty (helpful for debugging)
    * default: `false` (disabled)
* `framing(boolean value)`
    * enables/disables built-in packet-framing codec of netty 
    * default: `true` (enabled)
* `processing(boolean value)`
    * enables/disables asynchronous packet-processing by usage of `EventExecutorGroup`
    * default: `true` (enabled)
* `compression(Compression compression)`
    * Compressions: `GZIP`, `ZLIB`, `SNAPPY` and `NONE`
    * default: `Compression.NONE`
* `errorMode(ErrorMode errorMode)`
    * ErrorModes: `SILENT`, `STACK_TRACE`, `EVENT`
    * default: `ErrorMode.STACK_TRACE`
* `autoReconnect(int seconds)`
    * automatic reconnect after `int seconds`
    * default: `-1` (disabled)
    * `-1` to disable reconnect (or use `builder.disableReconnect()`)
* `useUDS(String udsFile)`
    * Try to use Unix-Domain-Sockets (short: `UDS`)
    * default: `null` (disabled)
    * default-path: `tmp/endpoint-netty/uds.sock`
* `password(String password)`
    * Automatic encryption
    * Algorithms: `AES-128` and `SHA-256`
    * default: `null` (disabled)

#### Timeout options:
* `timeout(int writeTimeout, int readTimeout)`
    * default: disabled (write-timeout: `15`, read-timeout: `0`) 
    * `0` = disabled
* `disableTimeouts()`
    * disable usage of `IdleStateHandler` and timeout-events 

What does mean `ReadTimeout` and `WriteTimeout`?

If after the time (`writeTimeout` in seconds) no object has been transferred 
from the client to the server, a WriteTimeout is thrown.

If after the time (`readTimeout` in seconds) no object has been transferred 
from the server to the client, a ReadTimeout is thrown.

Default behaviour:
* `WriteTimeout`
    * default: `int 1` is sent after timeout to keep alive the channel if no further listener is registered.
* `ReadTimeout`
    * default: no action

## How to build the Endpoints:

To build the `EndpointServer` or `EndpointClient`:
```java
EndpointBuilder builder = EndpointBuilder.builder()
        // Add more options by fluent calls
        .logging(false)
        .framing(true)
        .processing(true)
        .timeout(15, 0);

EndpointClient client = ClientBuilder.of(builder, String host, int port);

EndpointServer server = ServerBuilder.of(builder, int port);
```
    
## How to start the Endpoints

To start the `EndpointServer` or `EndpointClient` call `start()`. 

```java
EndpointBuilder builder = EndpointBuilder.builder()
        // Add more options by fluent calls
        .logging(false)
        .framing(true)
        .processing(true)
        .timeout(15, 0);

EndpointServer server = ServerBuilder.of(endpointBuilder, 54321);
server.start();

EndpointClient client = ClientBuilder.of(endpointBuilder, "localhost", 54321);
client.start();
```

## How to create Packets
This is a sample ``EndpointPacket`` with a ``String``, a ``long``, and a ``byte[]`` as attributes.
Each attribute of a ``EndpointPacket`` must be written/read independently to/from the ``ByteBuf``.
````java
public class TestRequest implements EndpointPacket {

    String testString;
    long testLong;
    byte[] testBytes;

    public String getTestString() {
        return testString;
    }

    public TestRequest setTestString(String testString) {
        this.testString = testString;
        return this;
    }

    public long getTestLong() {
        return testLong;
    }

    public TestRequest setTestLong(long testLong) {
        this.testLong = testLong;
        return this;
    }

    public byte[] getTestBytes() {
        return testBytes;
    }

    public TestRequest setTestBytes(byte[] testBytes) {
        this.testBytes = testBytes;
        return this;
    }

    @Override
    public void read(ByteBuf byteBuf) {
        this.testString = BufUtils.readString(byteBuf);
        this.testLong = BufUtils.readVarLong(byteBuf);
        this.testBytes = BufUtils.readArray(byteBuf);
    }

    @Override
    public void write(ByteBuf byteBuf) {
        BufUtils.writeString(testString, byteBuf);
        BufUtils.writeVarLong(testLong, byteBuf);
        BufUtils.writeArray(testBytes, byteBuf);
    }
}
````

# How to send Packets
To register a ``EndpointPacket``, you need a ``Supplier<? extends EndpointPacket>`` of the specific ``EndpointPacket`` so
that EndpointNetty can initialize new instances.
````java
EndpointBuilder builder = EndpointBuilder.builder()
    // Add more options by fluent calls
    .logging(false)
    .framing(true)
    .processing(true)
    .timeout(15, 0)
    // Register as much packets as you want.
    // But packetIds can only be registered once!    
    .registerPacket(1, TestRequest::new) 
    .registerPacket(2, new Supplier<TestRequest>() {
            @Override
            public TestRequest get() {
                return new TestRequest();
            }
    })
    .registerPacket(3, TestRequest.class); // REQUIRES EMPTY CONSTRUCTOR

EndpointServer server = ServerBuilder.of(endpointBuilder, 54321);
server.start();

EndpointClient client = ClientBuilder.of(endpointBuilder, "localhost", 54321);
client.start();

// Create a new instance of the packet and set the attributes
TestRequest request = new TestRequest()
        .setTestString(/* Any string here */)
        .setTestLong(/* Any long here */)
        .setTestBytes(/* Any byte[] here */);

// Packet sending by client
// Send the packet and do something with the ChannelFuture
ChannelFuture future = client.send(request);

// Send the packet and ignore result.
client.sendAndForget(request);


// Packet sending by server
// Send the packet and do something with the ChannelFuture
ChannelFuture future = server.send(channel, request);

// Send the packet and ignore result.
server.sendAndForget(channel, request);

// Send the packet and do something with the ChannelFuture
Map<String, ChannelFuture> futureMap = server.broadcast(request);

// Send the packet to each connected client and ignore result.
server.broadcastAndForget(request);
````

## Default Events

The event system is completely `Consumer`-based. These are the default events:

* `ChannelActionEvent`
  * server/client: channel change connection state
  * ChannelAction: `CONNECT`, `DISCONNECT`
  
* `ChannelTimeoutEvent`
  * server/client: read-/write-timeout
  * Timeout: `READ` or `WRITE`
  
* `EndpointActionEvent`
  * server/client: Thrown if some action happens on the endpoint
  * EndpointAction: `START`, `STOP`, `CLOSE` (`RECONNECT` only thrown by client)
  
* `ReceiveEvent`
  * server/client: receives packet from client/server
    
* `ErrorEvent`
  * server/client: exception occured (only is thrown by `ErrorMode.EVENT`)

* `LogEvent` / `DebugEvent`
  * server/client: something got logged
  
## Register Events
To register events, use the following instructions:
````java
EndpointBuilder builder = EndpointBuilder.builder()
        // Add more options by fluent calls
        .logging(false)
        .framing(true)
        .processing(true)
        .timeout(15, 0)
        // Register as much packets as you want.
        // But packetIds can only be registered once!    
        .registerPacket(1, TestRequest.class);

EndpointServer server = ServerBuilder.of(endpointBuilder, 54321);
server.start();

EndpointClient client = ClientBuilder.of(endpointBuilder, "localhost", 54321);
client.start();

client.registerEvent(ReceiveEvent.class, event -> {
    if(event.getTypeObject() instanceof TestRequest) {
        TestRequest request = event.getTypeObject();
    }
});

// See example below
ReceiveListener listener = new ReceiveListener(server);

server.unregisterEvent(ReceiveEvent.class, listener);
````
Or create a separate class as event-listener:
````java
public class ReceiveListener implements Consumer<ReceiveEvent> {
    
    public ReceiveListener(Endpoint endpoint) {
        endpoint.registerEvent(ReceiveEvent.class, this);
    }
    
    @Override
    public void accept(ReceiveEvent event) {
        if(event.getTypeObject() instanceof TestRequest) {
            TestRequest request = event.getTypeObject();
        }
    }
    
}
````

## Unregister Events

Events can also be unregistered, but for this you need the instance of the listener:
````java

ReceiveListener listener = new ReceiveListener(server);

server.unregisterEvent(ReceiveEvent.class, listener);
````

## Create new Events

Create a new class and define the required fields as follows:
````java
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
````

To fire an event, use the following method:
````java
TestEvent event = new TestEvent("abc", 123);

CompletableFuture<TestEvent> future = server.fireEvent(event);
````

You can also use the ``CompletableFuture<? extends ConsumerEvent>`` as a callback:
````java
TestEvent event = new TestEvent("abc", 123);

CompletableFuture<TestEvent> future = server.fireEvent(event);

future.whenComplete((event, error) -> {
    // Do something, after event got processed.
});
````

## Reconnecting with EndpointClient

If you want to reconnect a `EndpointClient`, do this:

* Initial connect the client
```java
EndpointClient client = new EndpointClient(endpointBuilder, "localhost", 54321);
client.start(); 
```
* Close/disconnect the client - do not call `stop()`
```java
client.close();
```

* (optional) Change address
```java
client.setAddress("localhost", 12345);
```
* Start again
```java
client.start();
```

If you call `client.stop()`, all events get unregistered.

## What is a Networkable

The ``Networkable`` object specifies the ``readStream(DataInputStream input)`` and ``writeStream(DataOutputStream output)`` methods. 
This allows the ``NetworkableEncoder`` to read/write instances of the object to/from a ``DataOutputStream``/``DataInputStream``.
The interface is defined as follows:

````java
public interface Networkable {

  void readStream(DataInputStream input) throws Exception;

  void writeStream(DataOutputStream output) throws Exception;

  default Primitive read(DataInputStream input, Class<Primitive> primitiveClass) { /*...*/ }

  default void write(DataOutputStream output, Object primitive) { /*...*/ }
  
  default byte[] readArray(DataInputStream input) { /*...*/ }
  
  default void writeArray(DataOutputStream output, byte[] bytes) { /*...*/ }

}
````

The ``read(DataInputStream input, Class<Primitive> primitiveClass)`` and ``write(DataOutputStream output, Object object)``
methods are declared as default in the ``Networkable`` interface, which saves some code.


**Attention, both methods can only work with java primitives!**

## How to create Networkables

To define a new ``Networkable`` object, the corresponding class must implement the ``Networkable`` interface. 
Then the data to be processed must be written or read from/to the respective stream.
````java
public class NetworkTestObject extends TestRequest implements Networkable {

    @Override
    public void readStream(DataInputStream input) throws Exception {
        setTestString(input.readUTF());
        setTestLong(read(input, Long.class));
        setTestBytes(readArray(input));
    }

    @Override
    public void writeStream(DataOutputStream output) throws Exception {
        output.writeUTF(getTestString());
        write(output, getTestLong());
        writeArray(output, getTestBytes());
    }
}
````

## How to encode or decode Networkable

Here is the example how to define the ``NetworkableEncoder`` and how to encode/decode with it.
````java
public class NetworkableExample {
    
    public static void main(String[] args) {
        NetworkableEncoder networkableEncoder = new NetworkableEncoder();
        networkableEncoder
              .register(1, new Supplier<Networkable>() {
                  @Override 
                  public Networkable get() {
                    return new NetworkTestObject();
                  }
              })
              .register(2, NetworkTestObject::new);
        byte[] objectEncoded = networkableEncoder.encode(networkTestObject);
        NetworkTestObject objectDecoded = networkableEncoder.decode(objectEncoded);
    }

}
````

**Attention: if no supplier is registered for the ``Networkable``, the ``NetworkableEncoder`` throws an ``NullPointerException``.**

## Add as dependency

Add `repo.koboo.eu` as repository. 

```java
repositories {
    maven { 
        url 'https://repo.koboo.eu/releases' 
    }
}
```

And add it as dependency. (e.g. `2.3` is the release-version)
```groovy
dependencies {
    // !Always needed! 
    compile 'eu.koboo:endpoint-core:2.6'
  
   // (optional) networkable-related
   compile 'eu.koboo:endpoint-networkable:2.6'
        
    // client-related     
    compile 'eu.koboo:endpoint-client:2.6'
        
    // server-related     
    compile 'eu.koboo:endpoint-server:2.6'
}
```

## Build from source

* Clone repository
* Run `./gradlew build`
* Output `/build/libs/endpoint-netty-{version}.jar`
