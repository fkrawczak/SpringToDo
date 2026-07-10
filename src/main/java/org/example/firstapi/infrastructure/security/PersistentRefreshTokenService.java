package org.example.firstapi.infrastructure.security;

import org.example.firstapi.application.security.RefreshTokenService;
import org.example.firstapi.application.exceptions.InvalidRefreshTokenException;
import org.example.firstapi.domain.model.refreshtoken.RefreshToken;
import org.example.firstapi.domain.model.refreshtoken.RefreshTokenRepository;
import org.example.firstapi.domain.model.user.User;
import org.example.firstapi.domain.shared.Clock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Component
public class PersistentRefreshTokenService implements RefreshTokenService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;
    private final Clock clock;
    private final long refreshTokenTtlSeconds;

    public PersistentRefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            Clock clock,
            @Value("${app.security.jwt.refresh-token-ttl-seconds}") long refreshTokenTtlSeconds
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.clock = clock;
        this.refreshTokenTtlSeconds = refreshTokenTtlSeconds;
    }

    @Override
    public String createFor(User user) {
        OffsetDateTime createdAt = clock.now();
        OffsetDateTime expiresAt = createdAt.plusSeconds(refreshTokenTtlSeconds);
        String token = createRefreshToken();
        String tokenHash = hash(token);

        refreshTokenRepository.save(new RefreshToken(user, tokenHash, createdAt, expiresAt));

        return token;
    }

    @Override
    public User getUserForValidToken(String token) {
        OffsetDateTime now = clock.now();

        return refreshTokenRepository.findByTokenHash(hash(token))
                .filter(refreshToken -> refreshToken.getExpiresAt().isAfter(now))
                .map(RefreshToken::getUser)
                .orElseThrow(InvalidRefreshTokenException::new);
    }

    private String createRefreshToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedToken = digest.digest(token.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hashedToken);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is not available", exception);
        }
    }
}
