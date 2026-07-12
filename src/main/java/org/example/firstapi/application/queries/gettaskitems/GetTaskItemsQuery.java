package org.example.firstapi.application.queries.gettaskitems;

import org.example.firstapi.domain.model.taskitem.TaskItemStatus;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

public record GetTaskItemsQuery(
        UUID userId,
        List<TaskItemStatus> statuses,
        int page,
        int size,
        String search
) {
    public GetTaskItemsQuery {
        statuses = statuses == null ? List.of() : List.copyOf(statuses);
        search = search == null || search.isBlank() ? null : search.trim().toLowerCase(Locale.ROOT);
    }
}
