package eu.koboo.endpoint.core.events.message;


import eu.koboo.event.CallableEvent;

public class LogEvent implements CallableEvent {

    private final String message;

    public LogEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
