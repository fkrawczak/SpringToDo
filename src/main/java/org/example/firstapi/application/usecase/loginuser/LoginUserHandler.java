package org.example.firstapi.application.usecase.loginuser;

import org.example.firstapi.application.exceptions.InvalidCredentialsException;
import org.example.firstapi.application.security.AccessTokenService;
import org.example.firstapi.application.security.RefreshTokenService;
import org.example.firstapi.application.core.EmailNormalizer;
import org.example.firstapi.domain.model.user.User;
import org.example.firstapi.domain.model.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginUserHandler {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccessTokenService accessTokenService;
    private final RefreshTokenService refreshTokenService;

    public LoginUserHandler(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AccessTokenService accessTokenService,
            RefreshTokenService refreshTokenService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.accessTokenService = accessTokenService;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public AuthTokens handle(LoginUserCommand command) {
        String email = EmailNormalizer.normalize(command.email());
        User user = userRepository.findByEmail(email)
                .filter(foundUser -> passwordEncoder.matches(command.password(), foundUser.getPassword()))
                .orElseThrow(InvalidCredentialsException::new);

        return new AuthTokens(
                accessTokenService.createFor(user),
                refreshTokenService.createFor(user)
        );
    }
}
