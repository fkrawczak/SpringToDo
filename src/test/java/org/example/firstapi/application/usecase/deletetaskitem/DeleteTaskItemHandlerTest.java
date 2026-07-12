package org.example.firstapi.application.usecase.deletetaskitem;

import org.example.firstapi.application.exceptions.TaskItemAccessDeniedException;
import org.example.firstapi.application.exceptions.TaskItemNotFoundException;
import org.example.firstapi.domain.model.taskitem.TaskItem;
import org.example.firstapi.domain.model.taskitem.TaskItemRepository;
import org.example.firstapi.domain.model.user.User;
import org.example.firstapi.domain.shared.Clock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteTaskItemHandlerTest {
    @Mock private TaskItemRepository taskItemRepository;
    @Mock private Clock clock;

    @Test
    void handleDeletesTaskWhenAuthenticatedUserIsItsAuthor() {
        // given
        User author = new User("author@example.com", "hash", "Jane", "Doe");
        TaskItem task = taskCreatedBy(author);
        when(taskItemRepository.findById(task.getId())).thenReturn(Optional.of(task));
        DeleteTaskItemHandler handler = new DeleteTaskItemHandler(taskItemRepository);

        // when
        handler.handle(new DeleteTaskItemCommand(task.getId(), author.getId()));

        // then
        verify(taskItemRepository).delete(task);
    }

    @Test
    void handleRejectsDeletionWhenAuthenticatedUserIsNotTheAuthor() {
        // given
        User author = new User("author@example.com", "hash", "Jane", "Doe");
        User anotherUser = new User("other@example.com", "hash", "John", "Doe");
        TaskItem task = taskCreatedBy(author);
        when(taskItemRepository.findById(task.getId())).thenReturn(Optional.of(task));
        DeleteTaskItemHandler handler = new DeleteTaskItemHandler(taskItemRepository);

        // when // then
        assertThatThrownBy(() -> handler.handle(new DeleteTaskItemCommand(task.getId(), anotherUser.getId())))
                .isInstanceOf(TaskItemAccessDeniedException.class);
        verify(taskItemRepository, never()).delete(task);
    }

    @Test
    void handleReportsNotFoundTaskWithoutAttemptingDeletion() {
        // given
        UUID taskId = UUID.fromString("2a47f6b0-134e-4349-b987-6758bcf1a74f");
        when(taskItemRepository.findById(taskId)).thenReturn(Optional.empty());
        DeleteTaskItemHandler handler = new DeleteTaskItemHandler(taskItemRepository);

        // when // then
        assertThatThrownBy(() -> handler.handle(new DeleteTaskItemCommand(taskId, UUID.randomUUID())))
                .isInstanceOf(TaskItemNotFoundException.class);
        verify(taskItemRepository, never()).delete(any(TaskItem.class));
    }

    private TaskItem taskCreatedBy(User author) {
        OffsetDateTime now = OffsetDateTime.parse("2026-07-12T10:00:00Z");
        when(clock.now()).thenReturn(now);

        return new TaskItem(author, "Buy milk", null, now.plusDays(1), clock);
    }
}
