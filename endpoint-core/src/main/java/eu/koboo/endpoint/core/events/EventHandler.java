package eu.koboo.endpoint.core.events;

import eu.koboo.endpoint.core.AbstractEndpoint;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class EventHandler {

    private final ConcurrentHashMap<Class<?>, List<Consumer<? extends ConsumerEvent>>> consumerMap = new ConcurrentHashMap<>();

    private final AbstractEndpoint endpoint;

    public EventHandler(AbstractEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    @SuppressWarnings("unchecked")
    public <T extends ConsumerEvent> void register(Class<T> eventClass, Consumer<? super T> consumer) {
        List<Consumer<? extends ConsumerEvent>> consumerList = new ArrayList<>();

        if (consumerMap.containsKey(eventClass))
            consumerList = consumerMap.get(eventClass);

        if (!consumerList.contains(consumer))
            consumerList.add((Consumer<? extends ConsumerEvent>) consumer);

        consumerMap.put(eventClass, consumerList);
    }

    public <T extends ConsumerEvent> boolean hasListener(Class<T> eventClass) {
        return !consumerMap.getOrDefault(eventClass, new ArrayList<>()).isEmpty();
    }

    public <T extends ConsumerEvent> CompletableFuture<T> fireEvent(T event) {
        if (event == null || !hasListener(event.getClass())) {
            return CompletableFuture.completedFuture(event);
        }
        return CompletableFuture.supplyAsync(() -> {
            List<Consumer<? extends ConsumerEvent>> unknownConsumerList = consumerMap.getOrDefault(event.getClass(), new ArrayList<>());
            if (!unknownConsumerList.isEmpty())
                for (Consumer<? extends ConsumerEvent> consumer : unknownConsumerList) {
                    @SuppressWarnings("unchecked")
                    Consumer<? super T> castedConsumer = (Consumer<? super T>) consumer;
                    castedConsumer.accept(event);
                }
            return event;
        }, endpoint.executorGroup());
    }

    public <T extends ConsumerEvent> void unregister(Class<T> eventClass, Consumer<T> consumer) {
        List<Consumer<? extends ConsumerEvent>> unknownConsumerList = consumerMap.getOrDefault(eventClass, null);
        if (unknownConsumerList != null && !unknownConsumerList.isEmpty())
            unknownConsumerList.remove(consumer);
    }
}