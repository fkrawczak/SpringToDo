package org.example.firstapi.application.queries.gettaskitem;

import java.util.UUID;

public record GetTaskItemQuery(UUID taskId, UUID userId) {
}
