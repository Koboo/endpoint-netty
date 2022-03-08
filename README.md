![Binflux-Netty](binflux-netty.png)

Endpoint-Netty offers a fast and secure way to define 
your own lightweight protocol with netty. 
The main functions are 
[the definition of the protocol, 
the encoding of the packets](https://github.com/Koboo/endpoint-netty/tree/master/endpoint-core/src/main/java/eu/koboo/endpoint/core/codec)
and the provision of a 
[consumer-based event system](https://github.com/Koboo/endpoint-netty/tree/master/endpoint-core/src/main/java/eu/koboo/endpoint/core/events).

Further information can be found in the [project wiki](https://github.com/Koboo/endpoint-netty/wiki).

## Overview

* [Documentation](https://github.com/Koboo/endpoint-netty/wiki)
* [Java Version](#java-version)
* [Dependency](#add-as-dependency)
* [Build From Source](#build-from-source)
* [Netty](https://netty.io)  

## Java Version

The current versions of Endpoint-Netty are built with the **OpenJDK 16** and targets **Java 8** to ensure backward compatibility. If you want to build Endpoint-Netty on a newer version, go to [Build](#build-from-source) and follow the steps.

## Add as dependency

Here you can see how to add Endpoint-Netty to your project. This example only includes [Gradle](https://gradle.org/) and [Maven](https://maven.apache.org/), 
but can also support other build systems. 

**Gradle:**
```groovy
repositories {
    maven { 
        url 'https://repo.koboo.eu/releases' 
    }
}

dependencies {
    // !Always needed! 
    compile 'eu.koboo:endpoint-core:2.7'
        
    // client-related     
    compile 'eu.koboo:endpoint-client:2.7'
        
    // server-related     
    compile 'eu.koboo:endpoint-server:2.7'
}
```
**Maven:**
````xml
<repositories>
   <repository>
      <id>repo.koboo.eu</id>
      <url>https://repo.koboo.eu/releases</url>
   </repository>
</repositories>

<dependencies>
    <!-- !Always needed! --> 
   <dependency>
      <groupId>eu.koboo</groupId>
      <artifactId>endpoint-core</artifactId>
      <version>2.7</version>
   </dependency>
   
    <!-- client-related -->     
   <dependency>
      <groupId>eu.koboo</groupId>
      <artifactId>endpoint-client</artifactId>
      <version>2.7</version>
   </dependency>
   
    <!-- server-related -->    
   <dependency>
      <groupId>eu.koboo</groupId>
      <artifactId>endpoint-server</artifactId>
      <version>2.7</version>
   </dependency>
<dependencies>
````

You can also add this repository to your project via [jitpack](https://jitpack.io/), but this can cause problems unless you know how jitpack behaves.

## Build from source

Endpoint-Netty uses Gradle (6.7) to build the jar files.

* Clone repository 
  * ``git clone https://github.com/Koboo/endpoint-netty``
  
* Checkout the branch you want to build (default: ``master``)
  * ``git checkout master``
  
* Execute the gradle build-routine
  * ``gradle build`` (depends on your os / ide)
