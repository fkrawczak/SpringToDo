package org.example.firstapi.application.usecase.deletetaskitem;

import org.example.firstapi.application.exceptions.TaskItemAccessDeniedException;
import org.example.firstapi.application.exceptions.TaskItemNotFoundException;
import org.example.firstapi.domain.model.taskitem.TaskItem;
import org.example.firstapi.domain.model.taskitem.TaskItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteTaskItemHandler {
    private final TaskItemRepository taskItemRepository;

    public DeleteTaskItemHandler(TaskItemRepository taskItemRepository) {
        this.taskItemRepository = taskItemRepository;
    }

    @Transactional
    public void handle(DeleteTaskItemCommand command) {
        TaskItem task = taskItemRepository.findById(command.taskId())
                .orElseThrow(() -> new TaskItemNotFoundException(command.taskId()));

        if (!task.getCreatedBy().getId().equals(command.userId())) {
            throw new TaskItemAccessDeniedException(command.taskId());
        }

        taskItemRepository.delete(task);
    }
}
