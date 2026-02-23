package com.imopro.application;

import com.imopro.domain.TaskItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository {
    List<TaskItem> findAll();

    Optional<TaskItem> findById(UUID id);

    void save(TaskItem task);

    void delete(UUID id);
}
