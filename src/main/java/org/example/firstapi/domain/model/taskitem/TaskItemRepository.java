package org.example.firstapi.domain.model.taskitem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TaskItemRepository extends JpaRepository<TaskItem, UUID> {

    @Query("""
            select count(t)
            from TaskItem t
            where t.createdBy.id = :userId
              and (:filterByStatuses = false or t.status in :statuses)
              and (cast(:search as string) is null
                   or lower(t.title) like concat('%', cast(:search as string), '%'))
            """)
    long countByFilters(@Param("userId") UUID userId,
                        @Param("statuses") List<TaskItemStatus> statuses,
                        @Param("filterByStatuses") boolean filterByStatuses,
                        @Param("search") String search);

    @Query("""
            select t
            from TaskItem t
            where t.createdBy.id = :userId
              and (:filterByStatuses = false or t.status in :statuses)
              and (cast(:search as string) is null
                   or lower(t.title) like concat('%', cast(:search as string), '%'))
            """)
    List<TaskItem> findByFilters(@Param("userId") UUID userId,
                                 @Param("statuses") List<TaskItemStatus> statuses,
                                 @Param("filterByStatuses") boolean filterByStatuses,
                                 @Param("search") String search,
                                 Pageable pageable);
}
