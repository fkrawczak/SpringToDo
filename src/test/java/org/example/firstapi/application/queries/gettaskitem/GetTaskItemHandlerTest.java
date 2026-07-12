package org.example.firstapi.application.queries.gettaskitem;

import org.example.firstapi.application.exceptions.TaskItemAccessDeniedException;
import org.example.firstapi.application.exceptions.TaskItemNotFoundException;
import org.example.firstapi.domain.model.taskitem.TaskItem;
import org.example.firstapi.domain.model.taskitem.TaskItemRepository;
import org.example.firstapi.domain.model.taskitem.TaskItemStatus;
import org.example.firstapi.domain.model.user.User;
import org.example.firstapi.domain.shared.Clock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetTaskItemHandlerTest {
    @Mock private TaskItemRepository taskItemRepository;
    @Mock private Clock clock;

    @Test
    void handleReturnsTaskWhenAuthenticatedUserIsItsAuthor() {
        // given
        User author = new User("author@example.com", "hash", "Jane", "Doe");
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-07-12T10:00:00Z");
        OffsetDateTime deadline = OffsetDateTime.parse("2026-07-13T10:00:00Z");
        when(clock.now()).thenReturn(createdAt);
        TaskItem task = new TaskItem(author, "Buy milk", "Two bottles", deadline, clock);
        when(taskItemRepository.findById(task.getId())).thenReturn(Optional.of(task));
        GetTaskItemHandler handler = new GetTaskItemHandler(taskItemRepository);

        // when
        TaskItemResult result = handler.handle(new GetTaskItemQuery(task.getId(), author.getId()));

        // then
        assertThat(result).isEqualTo(new TaskItemResult(task.getId(), "Buy milk", "Two bottles", deadline,
                TaskItemStatus.NEW, createdAt, createdAt));
    }

    @Test
    void handleRejectsReadWhenAuthenticatedUserIsNotTheAuthor() {
        // given
        User author = new User("author@example.com", "hash", "Jane", "Doe");
        User anotherUser = new User("other@example.com", "hash", "John", "Doe");
        OffsetDateTime now = OffsetDateTime.parse("2026-07-12T10:00:00Z");
        when(clock.now()).thenReturn(now);
        TaskItem task = new TaskItem(author, "Buy milk", null, now.plusDays(1), clock);
        when(taskItemRepository.findById(task.getId())).thenReturn(Optional.of(task));
        GetTaskItemHandler handler = new GetTaskItemHandler(taskItemRepository);

        // when // then
        assertThatThrownBy(() -> handler.handle(new GetTaskItemQuery(task.getId(), anotherUser.getId())))
                .isInstanceOf(TaskItemAccessDeniedException.class);
    }

    @Test
    void handleReportsNotFoundTask() {
        // given
        UUID taskId = UUID.fromString("2a47f6b0-134e-4349-b987-6758bcf1a74f");
        when(taskItemRepository.findById(taskId)).thenReturn(Optional.empty());
        GetTaskItemHandler handler = new GetTaskItemHandler(taskItemRepository);

        // when // then
        assertThatThrownBy(() -> handler.handle(new GetTaskItemQuery(taskId, UUID.randomUUID())))
                .isInstanceOf(TaskItemNotFoundException.class);
    }
}
