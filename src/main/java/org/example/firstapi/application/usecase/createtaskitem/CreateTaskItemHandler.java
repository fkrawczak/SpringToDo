package org.example.firstapi.application.usecase.createtaskitem;

import org.example.firstapi.domain.model.taskitem.TaskItem;
import org.example.firstapi.domain.model.taskitem.TaskItemRepository;
import org.example.firstapi.domain.model.user.User;
import org.example.firstapi.domain.model.user.UserRepository;
import org.example.firstapi.domain.shared.Clock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class CreateTaskItemHandler {
    private final UserRepository userRepository;
    private final TaskItemRepository taskItemRepository;
    private final Clock clock;

    public CreateTaskItemHandler(UserRepository userRepository, TaskItemRepository taskItemRepository, Clock clock) {
        this.userRepository = userRepository;
        this.taskItemRepository = taskItemRepository;
        this.clock = clock;
    }

    @Transactional
    public UUID handle(CreateTaskItemCommand command) {
        User author = userRepository.getReferenceById(command.authorId());
        TaskItem task = new TaskItem(author, command.title(), command.description(), command.deadline(), clock);

        return taskItemRepository.save(task).getId();
    }
}
