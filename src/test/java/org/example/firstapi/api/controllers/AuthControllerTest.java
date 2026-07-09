package org.example.firstapi.api.controllers;

import org.example.firstapi.api.ApiExceptionHandler;
import org.example.firstapi.application.exceptions.InvalidCredentialsException;
import org.example.firstapi.application.usecase.loginuser.AuthTokens;
import org.example.firstapi.application.usecase.loginuser.LoginUserCommand;
import org.example.firstapi.application.usecase.loginuser.LoginUserHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class AuthControllerTest {

    private LoginUserHandler loginUserHandler;
    private MockMvc mockMvc;
    private static final String loginPath = "/api/login";

    @BeforeEach
    void setUp() {
        loginUserHandler = mock(LoginUserHandler.class);
        mockMvc = standaloneSetup(new AuthController(loginUserHandler))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void loginReturnsTokensFromHandler() throws Exception {
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
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));

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
}
