package org.example.firstapi.api.contracts.response;

public record LoginUserResponse(
        String accessToken,
        String refreshToken
) {
}
