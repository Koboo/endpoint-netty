package eu.koboo.endpoint.core;

import eu.koboo.endpoint.core.builder.EndpointBuilder;
import eu.koboo.event.EventBus;
import eu.koboo.nettyutils.NettyType;

import java.util.concurrent.ExecutorService;

public interface Endpoint {

    /**
     * @return Returns the Endpoint-Options instance
     */
    EndpointBuilder builder();

    /**
     * @return Returns the Endpoint-NetworkEventManager instance
     */
    EventBus eventHandler();

    /**
     * @return Returns the Endpoint-ExecutorService instance
     */
    ExecutorService executor();

    /**
     * @return Starts the Endpoint-connection and returns true if was successful
     */
    boolean start();

    /**
     * @return Stops the Endpoint-connection and returns true if was successful
     */
    boolean stop();

    /**
     * @return closes the Endpoint-connection and returns true if was successful
     */
    boolean close();

    /**
     * @return true if Endpoint is client
     */
    boolean isClient();

    NettyType nettyType();

    /**
     * @return called on Exception
     */
    void onException(Class clazz, Throwable error);
}
