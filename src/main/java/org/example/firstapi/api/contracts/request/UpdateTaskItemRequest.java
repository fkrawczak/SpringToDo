package org.example.firstapi.api.contracts.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.firstapi.domain.model.taskitem.TaskItemStatus;

import java.time.OffsetDateTime;

public record UpdateTaskItemRequest(
        @NotBlank String title,
        @NotNull TaskItemStatus status,
        String description,
        @NotNull OffsetDateTime deadline
) {
}
