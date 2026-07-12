package org.example.firstapi.application.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public final class UserRegisteredEvent extends ApplicationEvent {
    private final String email;
    private final String firstName;
    private final String lastName;

    @JsonCreator
    public UserRegisteredEvent(
            @JsonProperty("email") String email,
            @JsonProperty("firstName") String firstName,
            @JsonProperty("lastName") String lastName
    ) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Override
    public EventTopic topic() {
        return EventTopic.USER_REGISTERED;
    }

    public String email() { return email; }
    public String firstName() { return firstName; }
    public String lastName() { return lastName; }

    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof UserRegisteredEvent event)) return false;
        return Objects.equals(email, event.email)
                && Objects.equals(firstName, event.firstName)
                && Objects.equals(lastName, event.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, firstName, lastName);
    }
}
