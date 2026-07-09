package org.example.firstapi.application.exceptions;

public class EmailAlreadyTakenException extends RuntimeException {

    public EmailAlreadyTakenException(String email) {
        super("Email is already taken: " + email);
    }
}
