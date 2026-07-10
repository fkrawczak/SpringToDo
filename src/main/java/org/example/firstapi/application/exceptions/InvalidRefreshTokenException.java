package org.example.firstapi.application.exceptions;

public class InvalidRefreshTokenException extends RuntimeException {

    public InvalidRefreshTokenException() {
        super("Refresh token is invalid or expired");
    }
}
