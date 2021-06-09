package eu.koboo.endpoint.core.util;

import io.netty.util.concurrent.FastThreadLocalThread;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class LocalThreadFactory implements ThreadFactory {

    final AtomicInteger threadNumber;
    final String threadPrefix;

    public LocalThreadFactory(String threadPrefix) {
        this.threadPrefix = threadPrefix;
        this.threadNumber = new AtomicInteger();
    }

    public Thread newThread(Runnable runnable) {
        return new FastThreadLocalThread(runnable, threadPrefix + "-" + threadNumber.getAndIncrement());
    }
}
