package org.example.firstapi.infrastructure.security;

import org.example.firstapi.application.security.AccessTokenService;
import org.example.firstapi.domain.model.user.User;
import org.example.firstapi.domain.shared.Clock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Component
public class JwtAccessTokenService implements AccessTokenService {

    private final JwtEncoder jwtEncoder;
    private final Clock clock;
    private final long accessTokenTtlSeconds;

    public JwtAccessTokenService(
            JwtEncoder jwtEncoder,
            Clock clock,
            @Value("${app.security.jwt.access-token-ttl-seconds}") long accessTokenTtlSeconds
    ) {
        this.jwtEncoder = jwtEncoder;
        this.clock = clock;
        this.accessTokenTtlSeconds = accessTokenTtlSeconds;
    }

    @Override
    public String createFor(User user) {
        OffsetDateTime now = clock.now();
        OffsetDateTime expiresAt = now.plusSeconds(accessTokenTtlSeconds);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .issuedAt(toInstant(now))
                .expiresAt(toInstant(expiresAt))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    private Instant toInstant(OffsetDateTime value) {
        return value.withOffsetSameInstant(ZoneOffset.UTC).toInstant();
    }
}
