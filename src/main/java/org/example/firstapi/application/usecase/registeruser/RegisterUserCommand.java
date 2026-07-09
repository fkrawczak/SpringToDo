package org.example.firstapi.application.usecase.registeruser;

public record RegisterUserCommand(
        String email,
        String firstName,
        String lastName,
        String password
) {
}
