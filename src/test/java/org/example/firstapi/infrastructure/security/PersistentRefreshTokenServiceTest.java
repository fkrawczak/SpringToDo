package org.example.firstapi.infrastructure.security;

import org.example.firstapi.domain.model.refreshtoken.RefreshToken;
import org.example.firstapi.domain.model.refreshtoken.RefreshTokenRepository;
import org.example.firstapi.domain.model.user.User;
import org.example.firstapi.domain.shared.Clock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersistentRefreshTokenServiceTest {

    @Mock
    RefreshTokenRepository refreshTokenRepository;

    @Mock
    Clock clock;

    @Test
    void createForPersistsHashedRefreshTokenAndReturnsPlainToken() throws Exception {
        // given
        PersistentRefreshTokenService service = new PersistentRefreshTokenService(refreshTokenRepository, clock, 3600);
        OffsetDateTime now = OffsetDateTime.of(2026, 7, 9, 12, 0, 0, 0, ZoneOffset.ofHours(2));
        User user = new User("user@example.com", "hashed-password", "Jane", "Doe");
        when(clock.now()).thenReturn(now);

        // when
        String token = service.createFor(user);

        // then
        assertThat(token)
                .isNotBlank()
                .doesNotContain("=");
        assertThat(token).matches("[A-Za-z0-9_-]+");

        ArgumentCaptor<RefreshToken> refreshTokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(refreshTokenCaptor.capture());
        RefreshToken savedToken = refreshTokenCaptor.getValue();
        assertThat(savedToken.getUser()).isSameAs(user);
        assertThat(savedToken.getTokenHash()).isEqualTo(sha256Hex(token));
        assertThat(savedToken.getTokenHash()).hasSize(64);
        assertThat(savedToken.getTokenHash()).isNotEqualTo(token);
        assertThat(savedToken.getCreatedAt()).isEqualTo(OffsetDateTime.parse("2026-07-09T10:00:00Z"));
        assertThat(savedToken.getExpiresAt()).isEqualTo(OffsetDateTime.parse("2026-07-09T11:00:00Z"));
    }

    private String sha256Hex(String token) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
    }
}
