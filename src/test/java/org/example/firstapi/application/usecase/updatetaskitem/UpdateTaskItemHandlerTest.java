package org.example.firstapi.application.usecase.updatetaskitem;

import org.example.firstapi.application.exceptions.TaskItemAccessDeniedException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateTaskItemHandlerTest {
    @Mock private TaskItemRepository taskItemRepository;
    @Mock private Clock clock;

    @Test
    void handleUpdatesTaskWhenAuthenticatedUserIsItsAuthor() {
        // given
        User author = new User("author@example.com", "hash", "Jane", "Doe");
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-07-12T10:00:00Z");
        OffsetDateTime updatedAt = OffsetDateTime.parse("2026-07-12T11:00:00Z");
        when(clock.now()).thenReturn(createdAt, updatedAt, updatedAt);
        TaskItem task = new TaskItem(author, "Buy milk", null, createdAt.plusDays(1), clock);
        when(taskItemRepository.findById(task.getId())).thenReturn(Optional.of(task));
        UpdateTaskItemHandler handler = new UpdateTaskItemHandler(taskItemRepository, clock);
        OffsetDateTime deadline = OffsetDateTime.parse("2030-08-01T12:00:00Z");

        // when
        handler.handle(new UpdateTaskItemCommand(task.getId(), author.getId(), "Buy bread",
                TaskItemStatus.IN_PROGRESS, "Whole grain", deadline));

        // then
        assertThat(task.getTitle()).isEqualTo("Buy bread");
        assertThat(task.getStatus()).isEqualTo(TaskItemStatus.IN_PROGRESS);
        assertThat(task.getDescription()).isEqualTo("Whole grain");
        assertThat(task.getDeadline()).isEqualTo(deadline);
        assertThat(task.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void handleRejectsUpdateWhenAuthenticatedUserIsNotTheAuthor() {
        // given
        User author = new User("author@example.com", "hash", "Jane", "Doe");
        User anotherUser = new User("other@example.com", "hash", "John", "Doe");
        OffsetDateTime now = OffsetDateTime.parse("2026-07-12T10:00:00Z");
        when(clock.now()).thenReturn(now);
        TaskItem task = new TaskItem(author, "Buy milk", null, now.plusDays(1), clock);
        when(taskItemRepository.findById(task.getId())).thenReturn(Optional.of(task));
        UpdateTaskItemHandler handler = new UpdateTaskItemHandler(taskItemRepository, clock);

        // when // then
        assertThatThrownBy(() -> handler.handle(new UpdateTaskItemCommand(task.getId(), anotherUser.getId(),
                "Buy bread", TaskItemStatus.DONE, null, now.plusDays(2))))
                .isInstanceOf(TaskItemAccessDeniedException.class);
        verify(taskItemRepository, never()).save(any(TaskItem.class));
        assertThat(task.getTitle()).isEqualTo("Buy milk");
        assertThat(task.getStatus()).isEqualTo(TaskItemStatus.NEW);
    }
}
