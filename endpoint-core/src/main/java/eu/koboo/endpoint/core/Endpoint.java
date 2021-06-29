package eu.koboo.endpoint.core;

import eu.koboo.endpoint.core.builder.EndpointBuilder;
import eu.koboo.endpoint.core.events.ConsumerEvent;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface Endpoint {

    /**
     * @return Returns the Endpoint-Builder instance
     */
    EndpointBuilder builder();

    <T extends ConsumerEvent> Endpoint registerEvent(Class<T> eventClass, Consumer<? super T> consumer);

    <T extends ConsumerEvent> Endpoint unregisterEvent(Class<T> eventClass, Consumer<T> consumer);

    <T extends ConsumerEvent> CompletableFuture<T> fireEvent(T event);

    <T extends ConsumerEvent> boolean hasListener(Class<T> eventClass);

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
    @SuppressWarnings("all")
    Endpoint onException(Class clazz, Throwable error);
}
