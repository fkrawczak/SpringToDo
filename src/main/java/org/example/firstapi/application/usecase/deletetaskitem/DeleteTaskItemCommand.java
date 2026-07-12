package org.example.firstapi.application.usecase.deletetaskitem;

import java.util.UUID;

public record DeleteTaskItemCommand(UUID taskId, UUID userId) {
}
