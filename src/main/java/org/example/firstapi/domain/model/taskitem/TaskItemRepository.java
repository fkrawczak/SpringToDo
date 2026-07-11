package org.example.firstapi.domain.model.taskitem;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface TaskItemRepository extends JpaRepository<TaskItem, UUID> {
}
