package org.example.firstapi.application.security;

public record AuthTokens(
        String accessToken,
        String refreshToken
) {
}
