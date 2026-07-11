package org.example.firstapi.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.firstapi.api.contracts.request.CreateTaskItemRequest;
import org.example.firstapi.api.contracts.response.CreateTaskItemResponse;
import org.example.firstapi.application.usecase.createtaskitem.CreateTaskItemCommand;
import org.example.firstapi.application.usecase.createtaskitem.CreateTaskItemHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tasks")
public class TaskItemController {
    private final CreateTaskItemHandler handler;

    public TaskItemController(CreateTaskItemHandler handler) {
        this.handler = handler;
    }

    @PostMapping
    @Operation(summary = "Create a task", security = @SecurityRequirement(name = "HTTP Bearer"))
    public ResponseEntity<CreateTaskItemResponse> create(@AuthenticationPrincipal Jwt jwt,
                                                          @Valid @RequestBody CreateTaskItemRequest request) {
        CreateTaskItemCommand command = new CreateTaskItemCommand(UUID.fromString(jwt.getSubject()),
                request.title(), request.description(), request.deadline());
        UUID taskId = handler.handle(command);

        return ResponseEntity.created(URI.create("/api/tasks/" + taskId)).body(new CreateTaskItemResponse(taskId));
    }
}
