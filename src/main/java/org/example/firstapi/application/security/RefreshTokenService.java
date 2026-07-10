package org.example.firstapi.application.security;

import org.example.firstapi.domain.model.user.User;

public interface RefreshTokenService {

    String createFor(User user);

    User getUserForValidToken(String token);
}
