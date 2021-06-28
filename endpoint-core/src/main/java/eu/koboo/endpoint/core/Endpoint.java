package eu.koboo.endpoint.core;

import eu.koboo.endpoint.core.builder.EndpointBuilder;
import eu.koboo.endpoint.core.events.EventHandler;

import java.util.concurrent.ExecutorService;

public interface Endpoint {

    /**
     * @return Returns the Endpoint-Builder instance
     */
    EndpointBuilder builder();

    /**
     * @return Returns the Endpoint-EventHandler instance
     */
    EventHandler eventHandler();

    /**
     * @return Returns the Endpoint-ExecutorService instance
     */
    ExecutorService executor();

    /**
     * @return Starts the connection and returns true if was successful
     */
    boolean start();

    /**
     * @return Stops the connection and returns true if was successful
     */
    boolean stop();

    /**
     * @return closes the connection and returns true if was successful
     */
    boolean close();

    /**
     * @return Check if client is connected to server/if server is bound to port
     */
    boolean isConnected();

    /**
     * @return true if Endpoint is client or server
     */
    boolean isClient();

    /**
     * Always called if an Exception is thrown
     */
    void onException(Class clazz, Throwable error);
}
