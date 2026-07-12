package org.example.firstapi.application.queries.gettaskitem;

import org.example.firstapi.application.exceptions.TaskItemAccessDeniedException;
import org.example.firstapi.application.exceptions.TaskItemNotFoundException;
import org.example.firstapi.application.dtos.TaskItemResult;
import org.example.firstapi.domain.model.taskitem.TaskItem;
import org.example.firstapi.domain.model.taskitem.TaskItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetTaskItemHandler {
    private final TaskItemRepository taskItemRepository;

    public GetTaskItemHandler(TaskItemRepository taskItemRepository) {
        this.taskItemRepository = taskItemRepository;
    }

    @Transactional(readOnly = true)
    public TaskItemResult handle(GetTaskItemQuery query) {
        TaskItem task = taskItemRepository.findById(query.taskId())
                .orElseThrow(() -> new TaskItemNotFoundException(query.taskId()));

        if (!task.getCreatedBy().getId().equals(query.userId())) {
            throw new TaskItemAccessDeniedException(query.taskId());
        }

        return new TaskItemResult(task.getId(), task.getTitle(), task.getDescription(), task.getDeadline(),
                task.getStatus(), task.getCreatedAt(), task.getUpdatedAt());
    }
}
