package eu.koboo.endpoint.core.events;

import io.netty.util.concurrent.FastThreadLocalThread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class EventThreadFactory implements ThreadFactory {

    private static final String THREAD_PREFIX = "EventExecutor";

    final AtomicInteger threadNumber = new AtomicInteger();

    public EventThreadFactory() {
    }

    public Thread newThread(Runnable runnable) {
        return new FastThreadLocalThread(runnable, THREAD_PREFIX + "-" + threadNumber.getAndIncrement());
    }

}