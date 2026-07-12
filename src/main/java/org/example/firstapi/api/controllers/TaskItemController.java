package org.example.firstapi.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.firstapi.api.contracts.request.CreateTaskItemRequest;
import org.example.firstapi.api.contracts.response.CreateTaskItemResponse;
import org.example.firstapi.application.usecase.createtaskitem.CreateTaskItemCommand;
import org.example.firstapi.application.usecase.createtaskitem.CreateTaskItemHandler;
import org.example.firstapi.application.usecase.deletetaskitem.DeleteTaskItemCommand;
import org.example.firstapi.application.usecase.deletetaskitem.DeleteTaskItemHandler;
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
    private final CreateTaskItemHandler createHandler;
    private final DeleteTaskItemHandler deleteHandler;

    public TaskItemController(CreateTaskItemHandler createHandler, DeleteTaskItemHandler deleteHandler) {
        this.createHandler = createHandler;
        this.deleteHandler = deleteHandler;
    }

    @PostMapping
    @Operation(summary = "Create a task", security = @SecurityRequirement(name = "HTTP Bearer"))
    public ResponseEntity<CreateTaskItemResponse> create(@AuthenticationPrincipal Jwt jwt,
                                                          @Valid @RequestBody CreateTaskItemRequest request) {
        CreateTaskItemCommand command = new CreateTaskItemCommand(UUID.fromString(jwt.getSubject()),
                request.title(), request.description(), request.deadline());
        UUID taskId = createHandler.handle(command);

        return ResponseEntity.created(URI.create("/api/tasks/" + taskId)).body(new CreateTaskItemResponse(taskId));
    }

    @DeleteMapping("/{taskId}")
    @Operation(summary = "Delete a task", security = @SecurityRequirement(name = "HTTP Bearer"))
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID taskId) {
        deleteHandler.handle(new DeleteTaskItemCommand(taskId, UUID.fromString(jwt.getSubject())));

        return ResponseEntity.noContent().build();
    }
}
