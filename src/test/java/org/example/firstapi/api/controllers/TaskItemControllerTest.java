package org.example.firstapi.api.controllers;

import org.example.firstapi.api.ApiExceptionHandler;
import org.example.firstapi.application.usecase.createtaskitem.CreateTaskItemCommand;
import org.example.firstapi.application.usecase.createtaskitem.CreateTaskItemHandler;
import org.example.firstapi.application.usecase.deletetaskitem.DeleteTaskItemCommand;
import org.example.firstapi.application.usecase.deletetaskitem.DeleteTaskItemHandler;
import org.example.firstapi.application.usecase.updatetaskitem.UpdateTaskItemCommand;
import org.example.firstapi.application.usecase.updatetaskitem.UpdateTaskItemHandler;
import org.example.firstapi.application.queries.gettaskitem.GetTaskItemHandler;
import org.example.firstapi.application.queries.gettaskitem.GetTaskItemQuery;
import org.example.firstapi.application.dtos.TaskItemResult;
import org.example.firstapi.application.dtos.PageResult;
import org.example.firstapi.application.queries.gettaskitems.GetTaskItemsHandler;
import org.example.firstapi.application.queries.gettaskitems.GetTaskItemsQuery;
import org.example.firstapi.domain.model.taskitem.TaskItemStatus;
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
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class TaskItemControllerTest {
    private CreateTaskItemHandler handler;
    private DeleteTaskItemHandler deleteHandler;
    private GetTaskItemHandler getHandler;
    private GetTaskItemsHandler getItemsHandler;
    private UpdateTaskItemHandler updateHandler;
    private MockMvc mockMvc;
    private UUID userId;

    @BeforeEach
    void setUp() {
        handler = mock(CreateTaskItemHandler.class);
        deleteHandler = mock(DeleteTaskItemHandler.class);
        getHandler = mock(GetTaskItemHandler.class);
        getItemsHandler = mock(GetTaskItemsHandler.class);
        updateHandler = mock(UpdateTaskItemHandler.class);
        userId = UUID.randomUUID();
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "HS256").subject(userId.toString())
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(300)).build();
        mockMvc = standaloneSetup(new TaskItemController(handler, deleteHandler, getHandler, getItemsHandler,
                        updateHandler))
                .setCustomArgumentResolvers(jwtResolver(jwt))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void updateReturnsNoContentAndPassesBodyWithAuthenticatedUserId() throws Exception {
        // given
        UUID taskId = UUID.fromString("2a47f6b0-134e-4349-b987-6758bcf1a74f");

        // when
        var result = mockMvc.perform(put("/api/tasks/{taskId}", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title":"Buy bread","status":"IN_PROGRESS","description":"Whole grain","deadline":"2030-08-01T12:00:00Z"}
                        """));

        // then
        result.andExpect(status().isNoContent());
        verify(updateHandler).handle(new UpdateTaskItemCommand(taskId, userId, "Buy bread",
                TaskItemStatus.IN_PROGRESS, "Whole grain", OffsetDateTime.parse("2030-08-01T12:00:00Z")));
    }


    @Test
    void getReturnsTaskAndPassesTaskAndAuthenticatedUserIds() throws Exception {
        // given
        UUID taskId = UUID.fromString("2a47f6b0-134e-4349-b987-6758bcf1a74f");
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-07-12T10:00:00Z");
        OffsetDateTime deadline = OffsetDateTime.parse("2030-07-13T10:00:00Z");
        TaskItemResult task = new TaskItemResult(taskId, "Buy milk", "Two bottles", deadline,
                TaskItemStatus.NEW, createdAt, createdAt);
        when(getHandler.handle(any(GetTaskItemQuery.class))).thenReturn(task);

        // when
        var result = mockMvc.perform(get("/api/tasks/{taskId}", taskId));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId.toString()))
                .andExpect(jsonPath("$.title").value("Buy milk"))
                .andExpect(jsonPath("$.description").value("Two bottles"))
                .andExpect(jsonPath("$.deadline").value("2030-07-13T10:00:00Z"))
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.createdAt").value("2026-07-12T10:00:00Z"))
                .andExpect(jsonPath("$.updatedAt").value("2026-07-12T10:00:00Z"));
        verify(getHandler).handle(new GetTaskItemQuery(taskId, userId));
    }

    @Test
    void getAllReturnsPageAndPassesFiltersAndAuthenticatedUserId() throws Exception {
        // given
        TaskItemResult task = new TaskItemResult(UUID.randomUUID(), "Buy milk", null,
                OffsetDateTime.parse("2030-07-13T10:00:00Z"), TaskItemStatus.NEW,
                OffsetDateTime.parse("2026-07-12T10:00:00Z"), OffsetDateTime.parse("2026-07-12T10:00:00Z"));
        when(getItemsHandler.handle(any(GetTaskItemsQuery.class))).thenReturn(new PageResult<>(List.of(task), 12));

        // when
        var result = mockMvc.perform(get("/api/tasks")
                .param("statuses", "NEW", "IN_PROGRESS")
                .param("page", "2")
                .param("size", "5")
                .param("search", " milk "));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].title").value("Buy milk"))
                .andExpect(jsonPath("$.total").value(12));
        verify(getItemsHandler).handle(new GetTaskItemsQuery(userId,
                List.of(TaskItemStatus.NEW, TaskItemStatus.IN_PROGRESS), 2, 5, " milk "));
    }

    @Test
    void getAllUsesPaginationDefaults() throws Exception {
        // given
        when(getItemsHandler.handle(any(GetTaskItemsQuery.class))).thenReturn(new PageResult<>(List.of(), 0));

        // when
        mockMvc.perform(get("/api/tasks")).andExpect(status().isOk());

        // then
        verify(getItemsHandler).handle(new GetTaskItemsQuery(userId, List.of(), 1, 10, null));
    }

    @Test
    void getAllRejectsInvalidPaginationBeforeCallingHandler() throws Exception {
        // when + then
        mockMvc.perform(get("/api/tasks").param("page", "0").param("size", "101"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(getItemsHandler);
    }

    @Test
    void deleteReturnsNoContentAndPassesTaskAndAuthenticatedUserIds() throws Exception {
        // given
        UUID taskId = UUID.fromString("2a47f6b0-134e-4349-b987-6758bcf1a74f");

        // when
        var result = mockMvc.perform(delete("/api/tasks/{taskId}", taskId));

        // then
        result.andExpect(status().isNoContent());
        verify(deleteHandler).handle(new DeleteTaskItemCommand(taskId, userId));
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
