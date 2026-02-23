package com.imopro.ui;

import com.imopro.application.TaskService;
import com.imopro.domain.TaskItem;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public class TaskViewModel {
    private final TaskService taskService;
    private final ObservableList<TaskItem> tasks = FXCollections.observableArrayList();
    private final FilteredList<TaskItem> filteredTasks = new FilteredList<>(tasks, t -> true);
    private final ObjectProperty<TaskItem> selectedTask = new SimpleObjectProperty<>();

    private final StringProperty searchQuery = new SimpleStringProperty("");
    private final StringProperty title = new SimpleStringProperty("");
    private final StringProperty description = new SimpleStringProperty("");
    private final StringProperty dueDate = new SimpleStringProperty("");
    private final StringProperty status = new SimpleStringProperty("TODO");
    private final StringProperty filterMode = new SimpleStringProperty("ALL");

    public TaskViewModel(TaskService taskService) {
        this.taskService = taskService;
        loadTasks();
        selectedTask.addListener((obs, oldVal, newVal) -> populateFields(newVal));
        searchQuery.addListener((obs, oldVal, newVal) -> applyFilters());
        filterMode.addListener((obs, oldVal, newVal) -> applyFilters());
    }

    public ObservableList<TaskItem> getTasks() { return filteredTasks; }
    public ObjectProperty<TaskItem> selectedTaskProperty() { return selectedTask; }
    public StringProperty searchQueryProperty() { return searchQuery; }
    public StringProperty titleProperty() { return title; }
    public StringProperty descriptionProperty() { return description; }
    public StringProperty dueDateProperty() { return dueDate; }
    public StringProperty statusProperty() { return status; }

    public void loadTasks() {
        List<TaskItem> list = taskService.listTasks();
        list.sort(Comparator.comparing(TaskItem::getDueDate, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(TaskItem::getCreatedAt, Comparator.reverseOrder()));
        tasks.setAll(list);
        applyFilters();
    }

    public void createTask() {
        TaskItem task = TaskItem.newTask();
        tasks.add(0, task);
        selectedTask.set(task);
    }

    public void saveSelectedTask() {
        TaskItem task = selectedTask.get();
        if (task == null) {
            return;
        }
        task.setTitle(title.get());
        task.setDescription(description.get());
        task.setDueDate(parseDate(dueDate.get()));
        if ("DONE".equalsIgnoreCase(status.get())) {
            task.setStatus("DONE");
            if (task.getCompletedAt() == null) {
                task.setCompletedAt(Instant.now());
            }
        } else {
            task.markTodo();
        }

        taskService.save(task);
        loadTasks();
        selectedTask.set(task);
    }

    public void markDone() {
        TaskItem task = selectedTask.get();
        if (task == null) {
            return;
        }
        task.markDone();
        status.set("DONE");
        taskService.save(task);
        loadTasks();
        selectedTask.set(task);
    }

    public void deleteSelectedTask() {
        TaskItem task = selectedTask.get();
        if (task == null) {
            return;
        }
        taskService.delete(task.getId());
        tasks.remove(task);
        selectedTask.set(null);
    }

    public void setFilterToday() { filterMode.set("TODAY"); applyFilters(); }
    public void setFilterOverdue() { filterMode.set("OVERDUE"); applyFilters(); }
    public void setFilterWeek() { filterMode.set("WEEK"); applyFilters(); }
    public void setFilterAll() { filterMode.set("ALL"); applyFilters(); }

    public String display(TaskItem task) {
        String due = task.getDueDate() == null ? "sans échéance" : task.getDueDate().toString();
        String badge = "DONE".equals(task.getStatus()) ? "✓" : "•";
        return badge + " " + task.displayTitle() + " (" + due + ")";
    }

    public javafx.beans.binding.BooleanBinding canActBinding() {
        return Bindings.createBooleanBinding(() -> selectedTask.get() != null, selectedTask);
    }

    private void populateFields(TaskItem task) {
        if (task == null) {
            title.set("");
            description.set("");
            dueDate.set("");
            status.set("TODO");
            return;
        }
        title.set(task.getTitle() == null ? "" : task.getTitle());
        description.set(task.getDescription() == null ? "" : task.getDescription());
        dueDate.set(task.getDueDate() == null ? "" : task.getDueDate().toString());
        status.set(task.getStatus() == null ? "TODO" : task.getStatus());
    }

    private void applyFilters() {
        String query = searchQuery.get() == null ? "" : searchQuery.get().toLowerCase();
        LocalDate today = LocalDate.now();
        LocalDate endWeek = today.plusDays(6);

        filteredTasks.setPredicate(task -> {
            boolean textOk = query.isBlank()
                    || contains(task.getTitle(), query)
                    || contains(task.getDescription(), query)
                    || contains(task.getStatus(), query);
            if (!textOk) {
                return false;
            }

            return switch (filterMode.get()) {
                case "TODAY" -> task.getDueDate() != null && task.getDueDate().isEqual(today);
                case "OVERDUE" -> task.getDueDate() != null && task.getDueDate().isBefore(today) && !"DONE".equals(task.getStatus());
                case "WEEK" -> task.getDueDate() != null
                        && (task.getDueDate().isEqual(today) || task.getDueDate().isAfter(today))
                        && (task.getDueDate().isEqual(endWeek) || task.getDueDate().isBefore(endWeek));
                default -> true;
            };
        });
    }

    private LocalDate parseDate(String raw) {
        try {
            return raw == null || raw.isBlank() ? null : LocalDate.parse(raw);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean contains(String value, String q) {
        return value != null && value.toLowerCase().contains(q);
    }
}
