package org.example.firstapi.application.usecase.createtaskitem;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CreateTaskItemCommand(UUID authorId, String title, String description, OffsetDateTime deadline) {
}
