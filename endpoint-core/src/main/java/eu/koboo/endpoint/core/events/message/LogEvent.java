package eu.koboo.endpoint.core.events.message;


import eu.koboo.endpoint.core.events.EventHandler;

public class LogEvent implements EventHandler.ConsumerEvent {

    private final String message;

    public LogEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
