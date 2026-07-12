package org.example.firstapi.application.queries.gettaskitems;

import org.example.firstapi.application.dtos.PageResult;
import org.example.firstapi.application.dtos.TaskItemResult;
import org.example.firstapi.domain.model.taskitem.TaskItem;
import org.example.firstapi.domain.model.taskitem.TaskItemFilter;
import org.example.firstapi.domain.model.taskitem.TaskItemPage;
import org.example.firstapi.domain.model.taskitem.TaskItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetTaskItemsHandler {
    private final TaskItemRepository taskItemRepository;

    public GetTaskItemsHandler(TaskItemRepository taskItemRepository) {
        this.taskItemRepository = taskItemRepository;
    }

    @Transactional(readOnly = true)
    public PageResult<TaskItemResult> handle(GetTaskItemsQuery query) {
        TaskItemPage page = taskItemRepository.findByFilters(new TaskItemFilter(
                query.userId(),
                query.statuses(),
                query.search(),
                query.page() - 1,
                query.size()
        ));

        var items = page.items()
                .stream()
                .map(GetTaskItemsHandler::toResult)
                .toList();

        return new PageResult<>(items, page.total());
    }

    private static TaskItemResult toResult(TaskItem task) {
        return new TaskItemResult(task.getId(), task.getTitle(), task.getDescription(), task.getDeadline(),
                task.getStatus(), task.getCreatedAt(), task.getUpdatedAt());
    }
}
