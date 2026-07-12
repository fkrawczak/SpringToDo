package org.example.firstapi.application.usecase.updatetaskitem;

import org.example.firstapi.application.exceptions.TaskItemAccessDeniedException;
import org.example.firstapi.application.exceptions.TaskItemNotFoundException;
import org.example.firstapi.domain.model.taskitem.TaskItem;
import org.example.firstapi.domain.model.taskitem.TaskItemRepository;
import org.example.firstapi.domain.model.user.User;
import org.example.firstapi.domain.shared.Clock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateTaskItemHandler {
    private final TaskItemRepository taskItemRepository;
    private final Clock clock;

    public UpdateTaskItemHandler(TaskItemRepository taskItemRepository, Clock clock) {
        this.taskItemRepository = taskItemRepository;
        this.clock = clock;
    }

    @Transactional
    public void handle(UpdateTaskItemCommand command) {
        TaskItem task = taskItemRepository.findById(command.taskId())
                .orElseThrow(() -> new TaskItemNotFoundException(command.taskId()));
        User author = task.getCreatedBy();

        if (!author.getId().equals(command.userId())) {
            throw new TaskItemAccessDeniedException(command.taskId());
        }

        task.updateDetails(command.title(), command.description(), command.deadline(), author, clock);
        task.changeStatus(command.status(), author, clock);
    }
}
