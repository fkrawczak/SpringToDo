package org.example.firstapi.api.controllers;

import org.example.firstapi.api.ApiExceptionHandler;
import org.example.firstapi.application.exceptions.EmailAlreadyTakenException;
import org.example.firstapi.application.usecase.registeruser.RegisterUserCommand;
import org.example.firstapi.application.usecase.registeruser.RegisterUserHandler;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class RegisterControllerTest {

    private RegisterUserHandler registerUserHandler;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        registerUserHandler = mock(RegisterUserHandler.class);
        mockMvc = standaloneSetup(new RegisterController(registerUserHandler))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void registerReturnsCreatedUserId() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        when(registerUserHandler.handle(any(RegisterUserCommand.class))).thenReturn(userId);

        // when + then
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

        // then
        ArgumentCaptor<RegisterUserCommand> commandCaptor = ArgumentCaptor.forClass(RegisterUserCommand.class);
        verify(registerUserHandler).handle(commandCaptor.capture());
        assertThat(commandCaptor.getValue())
                .isEqualTo(new RegisterUserCommand("user@example.com", "Jane", "Doe", "secret"));
    }

    @Test
    void registerAcceptsConfiguredFirstNameAndLastNameAliases() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        when(registerUserHandler.handle(any(RegisterUserCommand.class))).thenReturn(userId);

        // when + then
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

        // then
        ArgumentCaptor<RegisterUserCommand> commandCaptor = ArgumentCaptor.forClass(RegisterUserCommand.class);
        verify(registerUserHandler).handle(commandCaptor.capture());
        assertThat(commandCaptor.getValue().firstName()).isEqualTo("Jane");
        assertThat(commandCaptor.getValue().lastName()).isEqualTo("Doe");
    }

    @Test
    void registerReturnsConflictWhenEmailIsAlreadyTaken() throws Exception {
        // given
        when(registerUserHandler.handle(any(RegisterUserCommand.class)))
                .thenThrow(new EmailAlreadyTakenException("user@example.com"));

        // when + then
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
    void registerRejectsInvalidRequestBodyBeforeCallingHandler() throws Exception {
        // when + then
        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "bad-email",
                                  "firstName": "",
                                  "lastName": "",
                                  "password": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Request body validation failed"))
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.firstName").exists())
                .andExpect(jsonPath("$.errors.lastName").exists())
                .andExpect(jsonPath("$.errors.password").exists());

        // then
        verifyNoInteractions(registerUserHandler);
    }
}
