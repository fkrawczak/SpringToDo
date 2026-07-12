package org.example.firstapi.application.events;

public enum EventTopic {
    USER_REGISTERED("user-registered");

    private final String name;

    EventTopic(String name) {
        this.name = name;
    }

    public String topicName() {
        return name;
    }
}
