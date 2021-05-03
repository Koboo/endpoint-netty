package eu.koboo.endpoint.core;

import eu.koboo.endpoint.core.builder.EndpointBuilder;
import eu.koboo.event.EventBus;

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
     * @return initializes the Endpoint and returns true if was successful
     */
    boolean initialize();

    /**
     * @return true if Endpoint is client
     */
    boolean isClient();

    /**
     * @return called on Exception
     */
    void onException(Class clazz, Throwable error);
}
