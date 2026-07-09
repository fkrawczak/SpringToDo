package org.example.firstapi.application.security;

import org.example.firstapi.domain.model.user.User;

public interface AccessTokenService {

    String createFor(User user);
}
