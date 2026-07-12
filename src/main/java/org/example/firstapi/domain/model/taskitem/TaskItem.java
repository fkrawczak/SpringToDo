package org.example.firstapi.domain.model.taskitem;

import jakarta.persistence.*;
import org.example.firstapi.domain.model.user.User;
import org.example.firstapi.domain.shared.AbstractEntity;
import org.example.firstapi.domain.shared.Clock;
import org.example.firstapi.domain.validation.DomainValidation;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "task_items")
public class TaskItem extends AbstractEntity {

    @Id
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "updated_by", nullable = false)
    private User updatedBy;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private OffsetDateTime deadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskItemStatus status;

    protected TaskItem() {
    }

    public TaskItem(User author, String title, String description, OffsetDateTime deadline, Clock clock) {
        this.id = UUID.randomUUID();
        this.createdAt = now(clock);
        this.updatedAt = createdAt;
        this.createdBy = Objects.requireNonNull(author, "author cannot be null");
        this.updatedBy = author;
        this.title = DomainValidation.requireText(title, "title");
        this.description = description;
        this.deadline = DomainValidation.requireDateTime(deadline, "deadline");
        this.status = TaskItemStatus.NEW;
    }

    public void updateDetails(String title, String description, OffsetDateTime deadline, User updatedBy, Clock clock) {
        this.title = DomainValidation.requireText(title, "title");
        this.description = description;
        this.deadline = DomainValidation.requireDateTime(deadline, "deadline");
        this.updatedBy = Objects.requireNonNull(updatedBy, "updatedBy cannot be null");
        this.updatedAt = now(clock);
    }

    public void changeStatus(TaskItemStatus status, User updatedBy, Clock clock) {
        this.status = Objects.requireNonNull(status, "status cannot be null");
        this.updatedBy = Objects.requireNonNull(updatedBy, "updatedBy cannot be null");
        this.updatedAt = now(clock);
    }

    public UUID getId() {
        return id;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public User getUpdatedBy() {
        return updatedBy;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public OffsetDateTime getDeadline() {
        return deadline;
    }

    public TaskItemStatus getStatus() {
        return status;
    }

    private static OffsetDateTime now(Clock clock) {
        return Objects.requireNonNull(clock, "clock cannot be null")
                .now()
                .withOffsetSameInstant(ZoneOffset.UTC);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof TaskItem taskItem)) {
            return false;
        }
        return id != null && Objects.equals(id, taskItem.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
