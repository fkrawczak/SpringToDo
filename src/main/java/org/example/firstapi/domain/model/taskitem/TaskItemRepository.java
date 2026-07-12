package org.example.firstapi.domain.model.taskitem;

import java.util.Optional;
import java.util.UUID;

public interface TaskItemRepository {

    TaskItem save(TaskItem taskItem);

    Optional<TaskItem> findById(UUID id);

    void delete(TaskItem taskItem);

    TaskItemPage findByFilters(TaskItemFilter filter);
}
