package com.imopro.application;

import com.imopro.domain.TaskItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TaskService {
    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    public List<TaskItem> listTasks() {
        return repository.findAll();
    }

    public Optional<TaskItem> getTask(UUID id) {
        return repository.findById(id);
    }

    public void save(TaskItem task) {
        repository.save(task);
    }

    public void delete(UUID id) {
        repository.delete(id);
    }
}
