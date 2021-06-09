![Binflux-Netty](binflux-netty.png)

Endpoint-Netty allows the use of different serialization libraries or 
native read/write calls to automatically and efficiently transfer object 
graphs across the network by using [Netty](http://netty.io/).

Simply explained: Send (almost) every object back and forth between client or server.

## History 

The [original project](https://github.com/EsotericSoftware/kryonetty) was
developed by [EsotericSoftware](https://github.com/EsotericSoftware).
Since I made some explicit changes, I created a [fork](https://github.com/BinfluxDev/binflux-netty) to make my own customizations.
After some time and through more special projects,
I've decided to release a private customized version and continue working on it.

Now the project is called EndpointNetty.

## Overview

This overview offers a simple step by step guide to get started with binflux-netty.

#### Endpoints
  * [EndpointBuilder](#what-is-the-endpointbuilder)
  * [Basic](#basic-options)
  * [Timeouts](#timeout-options)
  * [Serialization](#serializer-options)
#### Usage
  * [Build Endpoints](#how-to-build-the-endpoints)
  * [Start Endpoints](#how-to-start-the-endpoints)
  * [Reconnect](#reconnecting-with-endpointclient)
  * [Default Events](#default-events)
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

#### Timeout options:
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
    * sets the specific `SerializerPool` with a `Serialization`-class
    * default: `JavaSerialization`

Example usage:
```java
endpointBuilder.serializer(new SerializerPool(KryoSerialization.class));
```

All serialization is based on [Serialization](https://github.com/Koboo/serilization). 
More documentation in the project there

## How to build the Endpoints:

To build the `EndpointServer` or `EndpointClient`:
```java
EndpointClient client = new EndpointClient(endpointBuilder, String host, int port);

EndpointServer server = new EndpointServer(endpointBuilder, int port);
```
    
## How to start the Endpoints

To start the `EndpointServer` or `EndpointClient` call `start()`. 

```java
EndpointServer server = new EndpointServer(endpointBuilder, 54321);
server.start();

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

The entire event system is based on the [EventBus](https://github.com/Koboo/event-bus).
For further documentation see the project there.

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
    // !Always needed! 
    compile 'eu.koboo:netty-core:2.0'
    
    // client-related     
    compile 'eu.koboo:netty-client:2.0'
        
    // server-related     
    compile 'eu.koboo:netty-server:2.0'
}
```

## Build from source

* Clone repository
* Run `./gradlew buildApp`
* Output `/build/libs/endpoint-netty-{version}-all.jar`
* Build task [build.gradle](https://github.com/Koboo/endpoint-netty/blob/master/build.gradle)
