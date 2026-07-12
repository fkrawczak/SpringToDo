package org.example.firstapi.application.dtos;

import org.example.firstapi.domain.model.taskitem.TaskItemStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TaskItemResult(
        UUID id,
        String title,
        String description,
        OffsetDateTime deadline,
        TaskItemStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
