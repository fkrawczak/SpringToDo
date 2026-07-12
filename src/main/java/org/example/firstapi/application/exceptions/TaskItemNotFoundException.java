package org.example.firstapi.application.exceptions;

import java.util.UUID;

public class TaskItemNotFoundException extends RuntimeException {
    public TaskItemNotFoundException(UUID taskId) {
        super("Task with id " + taskId + " was not found");
    }
}
