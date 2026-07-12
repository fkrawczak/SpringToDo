package org.example.firstapi.application.queries.gettaskitems;

import org.example.firstapi.application.dtos.PageResult;
import org.example.firstapi.application.dtos.TaskItemResult;
import org.example.firstapi.domain.model.taskitem.TaskItem;
import org.example.firstapi.domain.model.taskitem.TaskItemRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GetTaskItemsHandler {
    private final TaskItemRepository taskItemRepository;

    public GetTaskItemsHandler(TaskItemRepository taskItemRepository) {
        this.taskItemRepository = taskItemRepository;
    }

    @Transactional(readOnly = true)
    public PageResult<TaskItemResult> handle(GetTaskItemsQuery query) {
        boolean filterByStatuses = !query.statuses().isEmpty();
        long total = taskItemRepository.countByFilters(
                query.userId(), query.statuses(), filterByStatuses, query.search());
        List<TaskItemResult> items = taskItemRepository.findByFilters(
                        query.userId(),
                        query.statuses(),
                        filterByStatuses,
                        query.search(),
                        PageRequest.of(
                                query.page() - 1,
                                query.size(),
                                Sort.by(Sort.Direction.DESC, "createdAt")
                        )
                )
                .stream()
                .map(GetTaskItemsHandler::toResult)
                .toList();

        return new PageResult<>(items, total);
    }

    private static TaskItemResult toResult(TaskItem task) {
        return new TaskItemResult(task.getId(), task.getTitle(), task.getDescription(), task.getDeadline(),
                task.getStatus(), task.getCreatedAt(), task.getUpdatedAt());
    }
}
