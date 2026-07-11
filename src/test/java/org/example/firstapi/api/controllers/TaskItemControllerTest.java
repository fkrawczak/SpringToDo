package org.example.firstapi.api.controllers;

import org.example.firstapi.api.ApiExceptionHandler;
import org.example.firstapi.application.usecase.createtaskitem.CreateTaskItemCommand;
import org.example.firstapi.application.usecase.createtaskitem.CreateTaskItemHandler;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class TaskItemControllerTest {
    private CreateTaskItemHandler handler;
    private MockMvc mockMvc;
    private UUID userId;

    @BeforeEach
    void setUp() {
        handler = mock(CreateTaskItemHandler.class);
        userId = UUID.randomUUID();
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "HS256").subject(userId.toString())
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(300)).build();
        mockMvc = standaloneSetup(new TaskItemController(handler))
                .setCustomArgumentResolvers(jwtResolver(jwt))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void createReturnsCreatedTaskIdAndLocation() throws Exception {
        // given
        UUID taskId = UUID.fromString("2a47f6b0-134e-4349-b987-6758bcf1a74f");
        when(handler.handle(any(CreateTaskItemCommand.class))).thenReturn(taskId);

        // when
        var result = mockMvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON).content("""
                {"title":"Buy milk","description":"Two bottles","deadline":"2030-07-13T10:00:00Z"}
                """));

        // then
        result.andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/tasks/" + taskId))
                .andExpect(jsonPath("$.id").value(taskId.toString()));
        ArgumentCaptor<CreateTaskItemCommand> captor = ArgumentCaptor.forClass(CreateTaskItemCommand.class);
        verify(handler).handle(captor.capture());
        assertThat(captor.getValue()).isEqualTo(new CreateTaskItemCommand(userId, "Buy milk", "Two bottles",
                OffsetDateTime.parse("2030-07-13T10:00:00Z")));
    }

    @Test
    void createRejectsInvalidBodyBeforeCallingHandler() throws Exception {
        // given
        String invalidBody = "{\"title\":\"\",\"deadline\":null}";

        // when
        var result = mockMvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON).content(invalidBody));

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Request body validation failed"));
        verifyNoInteractions(handler);
    }

    private HandlerMethodArgumentResolver jwtResolver(Jwt jwt) {
        return new HandlerMethodArgumentResolver() {
            public boolean supportsParameter(@NonNull MethodParameter parameter) {
                return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
            }
            public Object resolveArgument(@NonNull MethodParameter parameter, ModelAndViewContainer container,
                                          @NonNull NativeWebRequest request, WebDataBinderFactory factory) {
                return jwt;
            }
        };
    }
}
