package org.example.firstapi.application.usecase.createtaskitem;

import org.example.firstapi.domain.model.taskitem.TaskItem;
import org.example.firstapi.domain.model.taskitem.TaskItemRepository;
import org.example.firstapi.domain.model.taskitem.TaskItemStatus;
import org.example.firstapi.domain.model.user.User;
import org.example.firstapi.domain.model.user.UserRepository;
import org.example.firstapi.domain.shared.Clock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateTaskItemHandlerTest {
    @Mock UserRepository userRepository;
    @Mock TaskItemRepository taskItemRepository;
    @Mock Clock clock;
    @InjectMocks CreateTaskItemHandler handler;

    @Test
    void handleCreatesNewTaskAssignedToAuthenticatedUser() {
        // given
        UUID userId = UUID.fromString("9c16a094-a12b-4e83-8cd6-da338498d591");
        User author = new User("user@example.com", "hash", "Jane", "Doe");
        OffsetDateTime now = OffsetDateTime.of(2026, 7, 11, 10, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime deadline = now.plusDays(2);
        when(userRepository.getReferenceById(userId)).thenReturn(author);
        when(clock.now()).thenReturn(now);
        when(taskItemRepository.save(any(TaskItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        UUID result = handler.handle(new CreateTaskItemCommand(userId, "Buy milk", "Two bottles", deadline));

        // then
        ArgumentCaptor<TaskItem> captor = ArgumentCaptor.forClass(TaskItem.class);
        verify(taskItemRepository).save(captor.capture());
        TaskItem task = captor.getValue();
        assertThat(result).isEqualTo(task.getId());
        assertThat(task.getCreatedBy()).isSameAs(author);
        assertThat(task.getUpdatedBy()).isSameAs(author);
        assertThat(task.getTitle()).isEqualTo("Buy milk");
        assertThat(task.getDescription()).isEqualTo("Two bottles");
        assertThat(task.getDeadline()).isEqualTo(deadline);
        assertThat(task.getStatus()).isEqualTo(TaskItemStatus.NEW);
        assertThat(task.getCreatedAt()).isEqualTo(now);
    }
}
