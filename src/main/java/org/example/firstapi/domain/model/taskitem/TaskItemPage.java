package org.example.firstapi.domain.model.taskitem;

import java.util.List;

public record TaskItemPage(List<TaskItem> items, long total) {
    public TaskItemPage {
        items = List.copyOf(items);
    }
}
