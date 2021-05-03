![Binflux-Netty](binflux-netty.png)

Endpoint-Netty allows the use of different serialization libraries 
to automatically and efficiently transfer object graphs across the network by using [Netty](http://netty.io/).
The [original project](https://github.com/EsotericSoftware/kryonetty) was a fork of 
[KryoNetty](https://github.com/Koboo/kryonetty), with the goal of creating a production-ready & more modular version.

Simply explained: Send (almost) every object back and forth between client or server.


## Overview

This overview offers a simple step by step guide to get started with binflux-netty.

#### Endpoint
  * [EndpointBuilder](#what-is-the-endpointbuilder)
    * [Basic](#basic-options)
    * [IdleState](#idlestate-options)
    * [Netty](#netty-options)
    * [Serialization](#serializer-options)
  * [Endpoints](#building-the-endpoints)
    * [Build Endpoints](#building-the-endpoints)
    * [Server](#how-to-start-the-server)
    * [Client](#how-to-start-the-client)
    * [Reconnect](#reconnecting-with-endpointclient)
    * [Channel-Pool](#connection-pooling)

#### Events
  * [Default Events](#default-events)
  * [Register Events](#register-events)
  * [Create Events](#creating-events)
  * [Handle Events](#throwing-and-consuming-events)

#### Build and Download
  * [Download](#add-as-dependency)  
  * [Build From Source](#build-from-source)


## What is the EndpointBuilder
`EndpointBuilder` passes options to endpoints. Create new Builder-instance:

```java
EndpointBuilder builder = EndpointBuilder.newBuilder();
```

#### Basic options:
* `logging(boolean value)` 
    * enables/disables netty-built-in `LoggingHandler.class` (helpful for debugging)
    * default: disabled
* `protocol(Protocol protocol)` 
    * Choose between `SERIALIZABLE`, `NATIVE`
    * default: Protocol.SERIALIZABLE
* `compression(Compression compression)`
    * Choose between compressions `GZIP`, `ZLIB`, `SNAPPY`
    * default: `Compression.GZIP`
* `errorMode(ErrorMode errorMode)`
    * Choose between modes `SILENT`, `STACK_TRACE`, `EVENT`
    * default: `ErrorMode.STACK_TRACE`
* `eventMode(EventMode eventMode)`
    * Choose between modes `SYNC`, `SERVICE`, `EVENT_LOOP`
    * default: `ErrorMode.SERVICE`

#### IdleState options:
* `idleState(int readTimeout, int writeTimeout)`
    * enables initialization of `NettyIdleHandler.class`
    * default: disabled 
    * default-write-time: 15
    * default-read-time: 0 (= disabled)

What does mean `ReadTimeout` and `WriteTimeout`?

If after the time (`writeTimeout` in seconds) no object has been transferred 
from the client to the server, a WriteTimeout is thrown.

If after the time (`readTimeout` in seconds) no object has been transferred 
from the server to the client, a ReadTimeout is thrown.

* `WriteTimeout`
    * default-action: `int 1` is sent after timeout.
* `ReadTimeout`
    * default-action: no action

(Note: only the client throws this events.)

#### Serializer options:
* `serializer(SerializerPool pool)` 
    * sets the specific `SerializerPool` with `Serialization`
    * default: `KryoSerialization`

For better performance the serializations are pooled with a `SerializerPool`. 

Example usage:
```java
endpointBuilder.serializer(new SerializerPool(KryoSerialization.class));
```

See more about serialization: [serilization](https://github.com/Koboo/serilization)

## Building the endpoints:

To build the client:
```java
EndpointClient client = new EndpointClient(endpointBuilder, String host, int port);
```
    
To build the server:
```java
EndpointServer server = new EndpointServer(endpointBuilder, int port);
```

## How to start the server

To start the `EndpointServer` call `start()`. 

```java
EndpointServer server = new EndpointServer(endpointBuilder, 54321);
server.start();
```

## How to start the client

To start the `EndpointClient` call `start()`.

```java
EndpointClient client = new EndpointClient(endpointBuilder, "localhost", 54321);
client.start();
```


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

## Default Events

The event system is completely `Consumer<T>` based. There are some default events:

* `ChannelActionEvent`
    * server/client: channel connects/disconnects
    
* `NativeReceiveEvent`/`SerializableEvent`
    * server/client: receives object from client/server by the specific protocol
    
* `ErrorEvent`
    * server/client: exception occured
    
* `TimeoutEvent` 
    * client: read-/write-timeout
    * type: read or write
    
* `EndpointEvent`
    * endpoint: the endpoint
    * action: start, stop, close, initialize

## Register Events

Create a new listener by using a `EventListener<? extends CallableEvent>`:

```java
public class ConnectionListener implements EventListener<ChannelActionEvent> {
    @Override
    public void onEvent(ChannelActionEvent connectEvent) {
        if(connectEvent.getAction == ChannelActionEvent.Actionn.CONNECT) {
            Channel channel = event.getChannel();
            System.out.println("Server: Client connected: " + channel.remoteAddress());
        }
    }
}
```

Register an event to an endpoint:

```java
server.eventHandler().register(new ConnectionConsumer());
```

Syntax of `register`:
* `register(EventListener<? extends CallableEvent> eventListener)`
* `register(EventPriority priority, Consumer<? extends CallableEvent> consumer)`
* `register(Consumer<? extends CallableEvent> consumer)`

Pass consumer directly into method:

```java
server.eventHandler().register((ChannelActionEvent) event -> {
    if(event.getAction == ChannelActionEvent.Action.CONNECT) {
        Channel channel = event.getChannel();
        System.out.println("Server: Client connected: " + channel.remoteAddress());
    }
});
```

Example-consumer of `SerializableReceiveEvent`:

```java
public class ReceiveConsumer implements EventListener<SerializableReceiveEvent> {
    @Override
    public void onEvent(SerializableReceiveEvent event) {
        Channel channel = event.getChannel();
        SerializablePacket object = event.getTypeObject();
        System.out.println("Server: Client received: " + channel.remoteAddress() + "/" + object);
        if (object instanceof Boolean) {
            Boolean result = (Boolean) object;
            System.out.println("Result is: " + result);
        }
    }
}
```


## Creating Events

If you want to create your own event and let the clients or servers handle it, an event could look like this:

```java
public class SampleEvent implements CallableEvent {

    private String string;
    private Integer value;
    private Long timeStamp;

    public SampleEvent(String string, Integer value, Long timeStamp) {
        this.string = string;
        this.value = value;
        this.timeStamp = timeStamp;
    } 
    
    public String getString() {
        return this.string;
    }

    public Integer getValue() {
        return this.value;
    }

    public Long getTimeStamp() {
        return this.timeStamp;
    }   

    @Override
    public String toString() {
        return "SampleEvent(string=" + this.string + "; " +
         "value=" + this.value + "; " +
          "timeStamp=" + this.timeStamp + ")";
    }
}
```


## Throwing and Consuming Events

To call the consumers of the event, you can pass the event to the `EventHandler` in an `Endpoint`.

Throw events:
```java
endpoint.eventHandler().callEvent(new SampleEvent("SampleString", 100, System.currentTimeMillis()));
```

Consume/Register events:
```java
endpoint.eventHandler().register((SampleEvent) event -> System.out.println(event.toString()));
```

## Add as dependency

Add `repo.koboo.eu` as repository. 

```java
repositories {
    maven { url 'https://repo.koboo.eu/releases' }
}
```

And add it as dependency. (e.g. `2.0` is the release-version)
```java
dependencies {
    compile 'eu.koboo:endpoint-netty:2.0'
}
```


## Build from source

* Clone repository
* Run `./gradlew buildApp`
* Output `/build/libs/endpoint-netty-{version}-all.jar`
* Build task [build.gradle](https://github.com/Koboo/endpoint-netty/blob/master/build.gradle)
