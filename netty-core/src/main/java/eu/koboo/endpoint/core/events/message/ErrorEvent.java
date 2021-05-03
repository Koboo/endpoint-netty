package eu.koboo.endpoint.core.events.message;

import eu.koboo.event.CallableEvent;

public class ErrorEvent implements CallableEvent {

    private final Class clazz;
    private final Throwable throwable;

    public ErrorEvent(Class clazz, Throwable throwable) {
        this.clazz = clazz;
        this.throwable = throwable;
    }

    public Class getClazz() {
        return clazz;
    }

    public Throwable getThrowable() {
        return throwable;
    }
}
