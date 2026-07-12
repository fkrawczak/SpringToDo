package org.example.firstapi.application.usecase.updatetaskitem;

import org.example.firstapi.domain.model.taskitem.TaskItemStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UpdateTaskItemCommand(UUID taskId, UUID userId, String title, TaskItemStatus status,
                                    String description, OffsetDateTime deadline) {
}
