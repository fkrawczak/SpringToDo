package org.example.firstapi.application.usecase.refreshaccesstoken;

import org.example.firstapi.application.exceptions.InvalidRefreshTokenException;
import org.example.firstapi.application.security.AccessTokenService;
import org.example.firstapi.application.security.RefreshTokenService;
import org.example.firstapi.domain.model.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshAccessTokenHandler {

    private final RefreshTokenService refreshTokenService;
    private final AccessTokenService accessTokenService;

    public RefreshAccessTokenHandler(RefreshTokenService refreshTokenService, AccessTokenService accessTokenService) {
        this.refreshTokenService = refreshTokenService;
        this.accessTokenService = accessTokenService;
    }

    @Transactional(readOnly = true)
    public String handle(RefreshAccessTokenCommand command) {
        if (command.refreshToken() == null || command.refreshToken().isBlank()) {
            throw new InvalidRefreshTokenException();
        }

        User user = refreshTokenService.getUserForValidToken(command.refreshToken());

        return accessTokenService.createFor(user);
    }
}
