package eu.koboo.endpoint.core.events;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class EventHandler {

    private final ConcurrentHashMap<Class<?>, List<Consumer<? extends ConsumerEvent>>> consumerMap;

    public EventHandler() {
        this.consumerMap = new ConcurrentHashMap<>();
    }

    public <T extends ConsumerEvent> void register(Class<T> eventClass, Consumer<? super T> consumer) {
        List<Consumer<? extends ConsumerEvent>> consumerList = new ArrayList<>();
        if (this.consumerMap.containsKey(eventClass))
            consumerList = this.consumerMap.get(eventClass);
        if (!consumerList.contains(consumer) )
            consumerList.add((Consumer<? extends ConsumerEvent>) consumer);
        this.consumerMap.put(eventClass, consumerList);
    }

    public boolean hasConsumer(Class<? extends Consumer<?>> eventClass) {
        return !this.consumerMap.getOrDefault(eventClass, new ArrayList<>()).isEmpty();
    }

    public <T extends ConsumerEvent> void handleEvent(T event) {
        List<Consumer<? extends ConsumerEvent>> unknownConsumerList = this.consumerMap.getOrDefault(event.getClass(), new ArrayList<>());
        if(!unknownConsumerList.isEmpty()) {
            for (Consumer<? extends ConsumerEvent> consumer : unknownConsumerList) {
                @SuppressWarnings("unchecked")
                Consumer<? super T> castedConsumer = (Consumer<? super T>) consumer;
                castedConsumer.accept(event);
            }
        }
    }

    public void unregisterAll() {
        this.consumerMap.clear();
    }

    public interface ConsumerEvent {

    }
}