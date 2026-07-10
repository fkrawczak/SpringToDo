package org.example.firstapi.application.usecase.refreshaccesstoken;

import org.example.firstapi.application.exceptions.InvalidRefreshTokenException;
import org.example.firstapi.application.security.AccessTokenService;
import org.example.firstapi.application.security.RefreshTokenService;
import org.example.firstapi.domain.model.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshAccessTokenHandlerTest {

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private AccessTokenService accessTokenService;

    @InjectMocks
    private RefreshAccessTokenHandler handler;

    @Test
    void issuesAccessTokenForUserOwningValidRefreshToken() {
        // given
        final String tokenFromCookie = "refresh-token";
        final String expectedAccessToken = "new-access-token";
        User user = new User("user@example.com", "hashed-password", "Jane", "Doe");
        when(refreshTokenService.getUserForValidToken(tokenFromCookie)).thenReturn(user);
        when(accessTokenService.createFor(user)).thenReturn(expectedAccessToken);

        // when
        String result = handler.handle(new RefreshAccessTokenCommand(tokenFromCookie));

        // then
        assertThat(result).isSameAs(expectedAccessToken);
        verify(accessTokenService).createFor(user);
    }

    @Test
    void rejectsMissingRefreshTokenWithoutIssuingAccessToken() {
        // when + then
        assertThatThrownBy(() -> handler.handle(new RefreshAccessTokenCommand(null)))
                .isInstanceOf(InvalidRefreshTokenException.class);
        verifyNoInteractions(refreshTokenService, accessTokenService);
    }
}
