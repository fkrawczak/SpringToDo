package org.example.firstapi.infrastructure.security;

import org.example.firstapi.domain.model.user.User;
import org.example.firstapi.domain.shared.Clock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAccessTokenServiceTest {

    @Mock
    private JwtEncoder jwtEncoder;

    @Mock
    private Clock clock;

    @Test
    void createForBuildsExpectedJwtClaimsAndReturnsEncodedTokenValue() {
        // given
        final String expectedToken = "encoded-access-token";
        final String userEmail = "user@example.com";
        JwtAccessTokenService service = new JwtAccessTokenService(jwtEncoder, clock, 900);
        OffsetDateTime now = OffsetDateTime.of(2026, 7, 9, 12, 30, 0, 0, ZoneOffset.ofHours(2));
        User user = new User(userEmail, "hashed-password", "Jane", "Doe");
        Jwt encodedJwt = Jwt.withTokenValue(expectedToken)
                .header("alg", "HS256")
                .subject(user.getId().toString())
                .build();

        when(clock.now()).thenReturn(now);
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(encodedJwt);

        // when
        String token = service.createFor(user);

        // then
        assertThat(token).isEqualTo(expectedToken);
        ArgumentCaptor<JwtEncoderParameters> parametersCaptor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder).encode(parametersCaptor.capture());
        JwtEncoderParameters parameters = parametersCaptor.getValue();
        assertThat(parameters.getJwsHeader().getAlgorithm()).isEqualTo(MacAlgorithm.HS256);
        assertThat(parameters.getClaims().getSubject()).isEqualTo(user.getId().toString());
        assertThat((String) parameters.getClaims().getClaim("email")).isSameAs(userEmail);
        assertThat(parameters.getClaims().getIssuedAt()).isEqualTo(Instant.parse("2026-07-09T10:30:00Z"));
        assertThat(parameters.getClaims().getExpiresAt()).isEqualTo(Instant.parse("2026-07-09T10:45:00Z"));
    }
}
