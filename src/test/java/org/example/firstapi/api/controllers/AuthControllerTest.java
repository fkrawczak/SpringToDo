package org.example.firstapi.api.controllers;

import org.example.firstapi.api.ApiExceptionHandler;
import org.example.firstapi.application.exceptions.InvalidCredentialsException;
import org.example.firstapi.application.exceptions.InvalidRefreshTokenException;
import org.example.firstapi.application.exceptions.EmailAlreadyTakenException;
import org.example.firstapi.application.usecase.loginuser.AuthTokens;
import org.example.firstapi.application.usecase.loginuser.LoginUserCommand;
import org.example.firstapi.application.usecase.loginuser.LoginUserHandler;
import org.example.firstapi.application.usecase.refreshaccesstoken.RefreshAccessTokenCommand;
import org.example.firstapi.application.usecase.refreshaccesstoken.RefreshAccessTokenHandler;
import org.example.firstapi.application.usecase.registeruser.RegisterUserCommand;
import org.example.firstapi.application.usecase.registeruser.RegisterUserHandler;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class AuthControllerTest {

    private LoginUserHandler loginUserHandler;
    private RefreshAccessTokenHandler refreshAccessTokenHandler;
    private RegisterUserHandler registerUserHandler;
    private MockMvc mockMvc;
    private static final String loginPath = "/api/login";

    @BeforeEach
    void setUp() {
        loginUserHandler = mock(LoginUserHandler.class);
        refreshAccessTokenHandler = mock(RefreshAccessTokenHandler.class);
        registerUserHandler = mock(RegisterUserHandler.class);
        mockMvc = standaloneSetup(new AuthController(loginUserHandler, refreshAccessTokenHandler,
                        registerUserHandler, 2_592_000))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void registerReturnsCreatedUserId() throws Exception {
        UUID userId = UUID.randomUUID();
        when(registerUserHandler.handle(any(RegisterUserCommand.class))).thenReturn(userId);

        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "firstName": "Jane",
                                  "lastName": "Doe",
                                  "password": "secret"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userId.toString()));

        ArgumentCaptor<RegisterUserCommand> captor = ArgumentCaptor.forClass(RegisterUserCommand.class);
        verify(registerUserHandler).handle(captor.capture());
        assertThat(captor.getValue())
                .isEqualTo(new RegisterUserCommand("user@example.com", "Jane", "Doe", "secret"));
    }

    @Test
    void registerAcceptsConfiguredFirstNameAndLastNameAliases() throws Exception {
        UUID userId = UUID.randomUUID();
        when(registerUserHandler.handle(any(RegisterUserCommand.class))).thenReturn(userId);

        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "first_name": "Jane",
                                  "lastname": "Doe",
                                  "password": "secret"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userId.toString()));

        ArgumentCaptor<RegisterUserCommand> captor = ArgumentCaptor.forClass(RegisterUserCommand.class);
        verify(registerUserHandler).handle(captor.capture());
        assertThat(captor.getValue().firstName()).isEqualTo("Jane");
        assertThat(captor.getValue().lastName()).isEqualTo("Doe");
    }

    @Test
    void registerReturnsConflictWhenEmailIsAlreadyTaken() throws Exception {
        when(registerUserHandler.handle(any(RegisterUserCommand.class)))
                .thenThrow(new EmailAlreadyTakenException("user@example.com"));

        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "firstName": "Jane",
                                  "lastName": "Doe",
                                  "password": "secret"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Email is already taken: user@example.com"));
    }

    @Test
    void loginReturnsAccessTokenAndStoresRefreshTokenInSecureHttpOnlyCookie() throws Exception {
        // given
        String expectedAccessToken = "access-token";
        String expectedRefreshToken = "refresh-token";
        when(loginUserHandler.handle(any(LoginUserCommand.class)))
                .thenReturn(new AuthTokens(expectedAccessToken, expectedRefreshToken));

        // when + then
        mockMvc.perform(post(loginPath)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "secret"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(cookie().value(AuthController.REFRESH_TOKEN_COOKIE, "refresh-token"))
                .andExpect(cookie().httpOnly(AuthController.REFRESH_TOKEN_COOKIE, true))
                .andExpect(cookie().secure(AuthController.REFRESH_TOKEN_COOKIE, true))
                .andExpect(cookie().path(AuthController.REFRESH_TOKEN_COOKIE, "/api"))
                .andExpect(cookie().maxAge(AuthController.REFRESH_TOKEN_COOKIE, 2_592_000))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("SameSite=Strict")));

        // then
        ArgumentCaptor<LoginUserCommand> commandCaptor = ArgumentCaptor.forClass(LoginUserCommand.class);
        verify(loginUserHandler).handle(commandCaptor.capture());
        assertThat(commandCaptor.getValue())
                .isEqualTo(new LoginUserCommand("user@example.com", "secret"));
    }

    @Test
    void loginReturnsUnauthorizedWhenCredentialsAreInvalid() throws Exception {
        // given
        when(loginUserHandler.handle(any(LoginUserCommand.class)))
                .thenThrow(new InvalidCredentialsException());

        // when + then
        mockMvc.perform(post(loginPath)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "wrong"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Invalid email or password"));
    }

    @Test
    void loginRejectsInvalidRequestBodyBeforeCallingHandler() throws Exception {
        // when + then
        mockMvc.perform(post(loginPath)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "not-an-email",
                                  "password": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Request body validation failed"))
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());

        // then
        verifyNoInteractions(loginUserHandler);
    }

    @Test
    void refreshIssuesNewAccessTokenUsingRefreshTokenCookie() throws Exception {
        // given
        String expectedAccessToken = "new-access-token";
        when(refreshAccessTokenHandler.handle(any(RefreshAccessTokenCommand.class))).thenReturn(expectedAccessToken);

        // when
        mockMvc.perform(post("/api/refresh")
                        .cookie(new Cookie(AuthController.REFRESH_TOKEN_COOKIE, "refresh-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(expectedAccessToken));

        // then
        ArgumentCaptor<RefreshAccessTokenCommand> captor = ArgumentCaptor.forClass(RefreshAccessTokenCommand.class);
        verify(refreshAccessTokenHandler).handle(captor.capture());
        assertThat(captor.getValue()).isEqualTo(new RefreshAccessTokenCommand("refresh-token"));
    }

    @Test
    void refreshReturnsUnauthorizedForInvalidRefreshToken() throws Exception {
        // given
        when(refreshAccessTokenHandler.handle(any(RefreshAccessTokenCommand.class)))
                .thenThrow(new InvalidRefreshTokenException());

        // when + then
        mockMvc.perform(post("/api/refresh")
                        .cookie(new Cookie(AuthController.REFRESH_TOKEN_COOKIE, "invalid-token")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Refresh token is invalid or expired"));
    }
}
