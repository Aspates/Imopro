package com.imopro.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class TaskItem {
    private final UUID id;
    private String title;
    private String description;
    private String status;
    private LocalDate dueDate;
    private final Instant createdAt;
    private Instant completedAt;
    private UUID rentId;
    private UUID rentRuleId;

    public TaskItem(UUID id,
                    String title,
                    String description,
                    String status,
                    LocalDate dueDate,
                    Instant createdAt,
                    Instant completedAt,
                    UUID rentId,
                    UUID rentRuleId) {
        this.id = Objects.requireNonNull(id, "id");
        this.title = title;
        this.description = description;
        this.status = status;
        this.dueDate = dueDate;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.completedAt = completedAt;
        this.rentId = rentId;
        this.rentRuleId = rentRuleId;
    }

    public static TaskItem newTask() {
        return new TaskItem(UUID.randomUUID(), "", "", "TODO", null, Instant.now(), null, null, null);
    }

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public UUID getRentId() { return rentId; }
    public void setRentId(UUID rentId) { this.rentId = rentId; }
    public UUID getRentRuleId() { return rentRuleId; }
    public void setRentRuleId(UUID rentRuleId) { this.rentRuleId = rentRuleId; }

    public void markDone() {
        this.status = "DONE";
        this.completedAt = Instant.now();
    }

    public void markTodo() {
        this.status = "TODO";
        this.completedAt = null;
    }

    public String displayTitle() {
        return (title == null || title.isBlank()) ? "(Tâche sans titre)" : title;
    }
}
