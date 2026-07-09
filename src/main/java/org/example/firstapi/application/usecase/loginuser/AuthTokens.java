package org.example.firstapi.application.usecase.loginuser;

public record AuthTokens(
        String accessToken,
        String refreshToken
) {
}
