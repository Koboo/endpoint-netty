package eu.koboo.endpoint.core.util;

import io.netty.util.concurrent.FastThreadLocalThread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class LocalThreadFactory implements ThreadFactory {

    final AtomicLong counter;
    final String prefix;

    public LocalThreadFactory(String threadPrefix) {
        prefix = threadPrefix;
        counter = new AtomicLong();
    }

    public Thread newThread(Runnable runnable) {
        return new FastThreadLocalThread(runnable, prefix + "-" + counter.getAndIncrement());
    }
}
