package org.example.firstapi.application.exceptions;

import java.util.UUID;

public class TaskItemAccessDeniedException extends RuntimeException {
    public TaskItemAccessDeniedException(UUID taskId) {
        super("Only the author can delete task with id " + taskId);
    }
}
