package org.example.firstapi.domain.model.taskitem;

import java.util.List;
import java.util.UUID;

public record TaskItemFilter(
        UUID userId,
        List<TaskItemStatus> statuses,
        String search,
        int page,
        int size
) {
    public TaskItemFilter {
        statuses = List.copyOf(statuses);
    }
}
