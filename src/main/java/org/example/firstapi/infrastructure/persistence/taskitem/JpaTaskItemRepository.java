package org.example.firstapi.infrastructure.persistence.taskitem;

import org.example.firstapi.domain.model.taskitem.TaskItem;
import org.example.firstapi.domain.model.taskitem.TaskItemFilter;
import org.example.firstapi.domain.model.taskitem.TaskItemPage;
import org.example.firstapi.domain.model.taskitem.TaskItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
class JpaTaskItemRepository implements TaskItemRepository {
    private final SpringDataTaskItemRepository repository;

    JpaTaskItemRepository(SpringDataTaskItemRepository repository) {
        this.repository = repository;
    }

    @Override
    public TaskItem save(TaskItem taskItem) {
        return repository.save(taskItem);
    }

    @Override
    public Optional<TaskItem> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public void delete(TaskItem taskItem) {
        repository.delete(taskItem);
    }

    @Override
    public TaskItemPage findByFilters(TaskItemFilter filter) {
        Page<TaskItem> result = repository.findByFilters(
                filter.userId(),
                filter.statuses(),
                !filter.statuses().isEmpty(),
                filter.search(),
                PageRequest.of(
                        filter.page(),
                        filter.size(),
                        Sort.by(Sort.Direction.DESC, "createdAt")
                )
        );

        return new TaskItemPage(result.getContent(), result.getTotalElements());
    }
}
