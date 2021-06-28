package eu.koboo.endpoint.core.events;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class EventHandler {

    private final ConcurrentHashMap<Class<?>, List<Consumer<? extends ConsumerEvent>>> consumerMap = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2, new EventThreadFactory());

    public EventHandler() {
        Thread shutdownThread = new Thread(executor::shutdown);
        shutdownThread.setName("EventExecutorShutdown");
        Runtime.getRuntime().addShutdownHook(shutdownThread);
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

    public <T extends ConsumerEvent> boolean hasConsumer(Class<T> eventClass) {
        return !consumerMap.getOrDefault(eventClass, new ArrayList<>()).isEmpty();
    }

    public <T extends ConsumerEvent> CompletableFuture<T> fireEvent(T event) {
        if (event == null || !hasConsumer(event.getClass())) {
            return CompletableFuture.completedFuture(event);
        }
        return CompletableFuture.supplyAsync(() -> handleEvent(event), executor);
    }

    private <T extends ConsumerEvent> T handleEvent(T event) {
        List<Consumer<? extends ConsumerEvent>> unknownConsumerList = consumerMap.getOrDefault(event.getClass(), new ArrayList<>());
        if (!unknownConsumerList.isEmpty())
            for (Consumer<? extends ConsumerEvent> consumer : unknownConsumerList) {
                @SuppressWarnings("unchecked")
                Consumer<? super T> castedConsumer = (Consumer<? super T>) consumer;
                castedConsumer.accept(event);
            }
        return event;
    }

    public <T extends ConsumerEvent> void unregister(Class<T> eventClass) {
        List<Consumer<? extends ConsumerEvent>> unknownConsumerList = consumerMap.getOrDefault(eventClass, null);
        if (unknownConsumerList != null && !unknownConsumerList.isEmpty())
            unknownConsumerList.clear();
        consumerMap.remove(eventClass);
    }

    public <T extends ConsumerEvent> void unregister(Class<T> eventClass, Consumer<T> consumer) {
        List<Consumer<? extends ConsumerEvent>> unknownConsumerList = consumerMap.getOrDefault(eventClass, null);
        if (unknownConsumerList != null && !unknownConsumerList.isEmpty())
            unknownConsumerList.remove(consumer);
    }
}