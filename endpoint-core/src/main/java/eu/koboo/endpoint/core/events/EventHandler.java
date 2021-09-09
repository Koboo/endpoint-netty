package eu.koboo.endpoint.core.events;

import eu.koboo.endpoint.core.AbstractEndpoint;
import eu.koboo.endpoint.core.EndpointCore;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@SuppressWarnings("all")
public class EventHandler {

    private final ConcurrentHashMap<Class<?>, List<Consumer<? extends ConsumerEvent>>> consumerMap = new ConcurrentHashMap<>();

    private final ExecutorService executor = Executors.newFixedThreadPool(EndpointCore.CORES * 4);

    public <T extends ConsumerEvent> void register(Class<T> eventClass, Consumer<? super T> consumer) {
        List<Consumer<? extends ConsumerEvent>> consumerList = consumerMap.get(eventClass);

        if (consumerList == null) {
            consumerList = new ArrayList<>();
        }

        if (!consumerList.contains(consumer)) {
            consumerList.add((Consumer<? extends ConsumerEvent>) consumer);
        }

        if (!consumerMap.containsKey(eventClass)) {
            consumerMap.put(eventClass, consumerList);
        }
    }

    public <T extends ConsumerEvent> boolean hasListener(Class<T> eventClass) {
        List<Consumer<? extends ConsumerEvent>> consumerList = consumerMap.get(eventClass);
        return consumerList != null && !consumerList.isEmpty();
    }

    public <T extends ConsumerEvent> CompletableFuture<T> fireEvent(T event) {
        if (event == null || !hasListener(event.getClass())) {
            return CompletableFuture.completedFuture(event);
        }
        if(executor.isShutdown() || executor.isTerminated()) {
            return CompletableFuture.completedFuture(event);
        }
        return CompletableFuture.supplyAsync(() -> {
            List<Consumer<? extends ConsumerEvent>> unknownConsumerList = consumerMap.get(event.getClass());
            if (unknownConsumerList != null && !unknownConsumerList.isEmpty()) {
                for (Consumer<? extends ConsumerEvent> consumer : unknownConsumerList) {
                    Consumer<? super T> castedConsumer = (Consumer<? super T>) consumer;
                    castedConsumer.accept(event);
                }
            }
            return event;
        }, executor);
    }

    public <T extends ConsumerEvent> void unregister(Class<T> eventClass, Consumer<T> consumer) {
        List<Consumer<? extends ConsumerEvent>> unknownConsumerList = consumerMap.get(eventClass);
        if (unknownConsumerList != null && !unknownConsumerList.isEmpty()) {
            unknownConsumerList.remove(consumer);
        }
    }

    public void shutdown() {
        executor.shutdown();
    }
}