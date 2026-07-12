package org.example.firstapi.application.queries.gettaskitems;

import org.example.firstapi.application.dtos.PageResult;
import org.example.firstapi.application.dtos.TaskItemResult;
import org.example.firstapi.domain.model.taskitem.TaskItem;
import org.example.firstapi.domain.model.taskitem.TaskItemPage;
import org.example.firstapi.domain.model.taskitem.TaskItemRepository;
import org.example.firstapi.domain.model.user.User;
import org.example.firstapi.domain.shared.Clock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetTaskItemsHandlerTest {
    @Mock private TaskItemRepository taskItemRepository;

    @Test
    void handleReturnsTaskWhenAuthenticatedUserIsItsAuthor() {
        // given
        GetTaskItemsQuery query = new GetTaskItemsQuery(
                UUID.fromString("d0555142-d01c-4bab-a4d2-80d268208957"),
                List.of(),
                1,
                10,
                null
        );
        GetTaskItemsHandler handler = new GetTaskItemsHandler(taskItemRepository);
        TaskItem resultTask = createTask();
        TaskItemResult expectedItem = new TaskItemResult(resultTask.getId(), resultTask.getTitle(),
                resultTask.getDescription(), resultTask.getDeadline(), resultTask.getStatus(),
                resultTask.getCreatedAt(), resultTask.getUpdatedAt());
        when(taskItemRepository.findByFilters(any()))
                .thenReturn(new TaskItemPage(List.of(resultTask), 1L));

        // when
        PageResult<TaskItemResult> result = handler.handle(query);

        // then
        assertThat(result.total()).isSameAs(1L);
        assertThat(result.items().getFirst()).isEqualTo(expectedItem);
    }

    private TaskItem createTask() {
        Clock clock = mock(Clock.class);
        when(clock.now()).thenReturn(OffsetDateTime.parse("2026-07-13T10:00:00Z"));
        User author = new User("author@example.com", "hash", "Jane", "Doe");
        OffsetDateTime deadline = OffsetDateTime.parse("2026-07-13T10:00:00Z");

        return new TaskItem(author, "Buy milk", "Two bottles", deadline, clock);
    }
}
