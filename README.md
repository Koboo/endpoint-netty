![Binflux-Netty](binflux-netty.png)

Endpoint-Netty facilitates the integration of a lightweight and easy to use packet-protocol into 
an environment supported by Netty.

Simply explained: Send (almost) every object back and forth between client or server.

## History 

The [original project](https://github.com/EsotericSoftware/kryonetty) was
developed by [EsotericSoftware](https://github.com/EsotericSoftware).
Since I made some explicit changes, I created a [fork](https://github.com/BinfluxDev/binflux-netty) to make my own customizations.
After some time and through more special projects,
I've decided to release a private customized version and continue working on it.

Now the project is called EndpointNetty.

## Overview

#### Endpoints
  * [EndpointBuilder](#what-is-the-endpointbuilder)
  * [Basic](#basic-options)
  * [Timeouts](#timeout-options)
#### Usage
  * [Build Endpoints](#how-to-build-the-endpoints)
  * [Start Endpoints](#how-to-start-the-endpoints)
  * [Create Packets](#how-to-create-packets)
  * [Default Events](#default-events)
  * [Register Events](#register-events)
  * [Reconnect](#reconnecting-with-endpointclient)
#### Build and Download
  * [Download](#add-as-dependency)  
  * [Build From Source](#build-from-source)


## What is the EndpointBuilder
`EndpointBuilder` passes options to endpoints. Create new Builder-instance:

```java
EndpointBuilder builder = EndpointBuilder.builder()
        // Add more options by fluent calls
        .logging(false)
        .timeout(15, 0);
```

#### Basic options:
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
        .timeout(15, 0);

EndpointClient client = new EndpointClient(builder, String host, int port);

EndpointServer server = new EndpointServer(builder, int port);
```
    
## How to start the Endpoints

To start the `EndpointServer` or `EndpointClient` call `start()`. 

```java
EndpointBuilder builder = EndpointBuilder.builder()
        // Add more options by fluent calls
        .logging(false)
        .timeout(15, 0);

EndpointServer server = new EndpointServer(endpointBuilder, 54321);
server.start();

EndpointClient client = new EndpointClient(endpointBuilder, "localhost", 54321);
client.start();
```

## How to create Packets

````java
public class TestRequest implements NativePacket {

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

````java
```java
EndpointBuilder builder = EndpointBuilder.builder()
    // Add more options by fluent calls
    .logging(false)
    .timeout(15, 0)
    // Register as much packets as you want.
    // But packetIds can only be registered once!    
    .registerPacket(1, TestRequest.class);

EndpointServer server = new EndpointServer(endpointBuilder, 54321);
server.start();

EndpointClient client = new EndpointClient(endpointBuilder, "localhost", 54321);
client.start();
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

````java
EndpointBuilder builder = EndpointBuilder.builder()
        // Add more options by fluent calls
        .logging(false)
        .timeout(15, 0)
        // Register as much packets as you want.
        // But packetIds can only be registered once!    
        .registerPacket(1, TestRequest.class);

EndpointServer server = new EndpointServer(endpointBuilder, 54321);
server.start();

EndpointClient client = new EndpointClient(endpointBuilder, "localhost", 54321);
client.start();

client.eventHandler().register(ReceiveEvent.class, event -> {
    if(event.getTypeObject() instanceof TestRequest) {
        TestRequest request = event.getTypeObject();
    }
});

new ReceiveListener(server);
````


````java
public class ReceiveListener implements Consumer<ReceiveEvent> {
    
    public ReceiveListener(Endpoint endpoint) {
        endpoint.eventHandler().register(ReceiveEvent.class, this);
    }
    
    @Override
    public void accept(ReceiveEvent event) {
        if(event.getTypeObject() instanceof TestRequest) {
            TestRequest request = event.getTypeObject();
        }
    }
    
}
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

## Add as dependency

Add `repo.koboo.eu` as repository. 

```java
repositories {
    maven { url 'https://repo.koboo.eu/releases' }
}
```

And add it as dependency. (e.g. `2.3` is the release-version)
```java
dependencies {
    // !Always needed! 
    compile 'eu.koboo:endpoint-core:2.3'
        
    // client-related     
    compile 'eu.koboo:endpoint-client:2.3'
        
    // server-related     
    compile 'eu.koboo:endpoint-server:2.3'
}
```

## Build from source

* Clone repository
* Run `./gradlew build`
* Output `/build/libs/endpoint-netty-{version}.jar`
