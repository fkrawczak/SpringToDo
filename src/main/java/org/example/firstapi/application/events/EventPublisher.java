package org.example.firstapi.application.events;

public interface EventPublisher {
    <T extends ApplicationEvent> void publish(T event);
}
