package org.example.firstapi.application.usecase.loginuser;

import org.example.firstapi.application.exceptions.InvalidCredentialsException;
import org.example.firstapi.application.security.AccessTokenService;
import org.example.firstapi.application.security.RefreshTokenService;
import org.example.firstapi.domain.model.user.User;
import org.example.firstapi.domain.model.user.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginUserHandlerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AccessTokenService accessTokenService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private LoginUserHandler handler;

    @Test
    void handleReturnsTokensWhenEmailAndPasswordMatch() {
        // given
        User user = new User("user@example.com", "hashed-password", "Jane", "Doe");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("raw-password", "hashed-password")).thenReturn(true);
        when(accessTokenService.createFor(user)).thenReturn("access-token");
        when(refreshTokenService.createFor(user)).thenReturn("refresh-token");

        // when
        AuthTokens tokens = handler.handle(new LoginUserCommand(" USER@EXAMPLE.COM ", "raw-password"));

        // then
        assertThat(tokens).isEqualTo(new AuthTokens("access-token", "refresh-token"));
        verify(userRepository).findByEmail("user@example.com");
        verify(accessTokenService).createFor(user);
        verify(refreshTokenService).createFor(user);
    }

    @Test
    void handleThrowsInvalidCredentialsWhenUserDoesNotExist() {
        // given
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        // when + then
        assertThatThrownBy(() -> handler.handle(new LoginUserCommand("missing@example.com", "raw-password")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");

        // then
        verifyNoInteractions(passwordEncoder, accessTokenService, refreshTokenService);
    }

    @Test
    void handleThrowsInvalidCredentialsWhenPasswordDoesNotMatch() {
        // given
        User user = new User("user@example.com", "hashed-password", "Jane", "Doe");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);

        // when + then
        assertThatThrownBy(() -> handler.handle(new LoginUserCommand("user@example.com", "wrong-password")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");

        // then
        verifyNoInteractions(accessTokenService, refreshTokenService);
    }
}
