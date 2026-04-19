package com.imopro.ui;

import com.imopro.application.TaskService;
import com.imopro.application.RentService;
import com.imopro.domain.RentTaskRule;
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
import java.util.UUID;

public class TaskViewModel {
    private final TaskService taskService;
    private final RentService rentService;
    private final ObservableList<TaskItem> tasks = FXCollections.observableArrayList();
    private final FilteredList<TaskItem> filteredTasks = new FilteredList<>(tasks, t -> true);
    private final ObjectProperty<TaskItem> selectedTask = new SimpleObjectProperty<>();

    private final StringProperty searchQuery = new SimpleStringProperty("");
    private final StringProperty title = new SimpleStringProperty("");
    private final StringProperty description = new SimpleStringProperty("");
    private final ObjectProperty<LocalDate> dueDate = new SimpleObjectProperty<>();
    private final StringProperty status = new SimpleStringProperty("TODO");
    private final StringProperty type = new SimpleStringProperty("-");
    private final StringProperty renewable = new SimpleStringProperty("-");
    private final StringProperty filterMode = new SimpleStringProperty("ALL");

    public TaskViewModel(TaskService taskService, RentService rentService) {
        this.taskService = taskService;
        this.rentService = rentService;
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
    public ObjectProperty<LocalDate> dueDateProperty() { return dueDate; }
    public StringProperty statusProperty() { return status; }
    public StringProperty typeProperty() { return type; }
    public StringProperty renewableProperty() { return renewable; }

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
        task.setDueDate(dueDate.get());
        if ("DONE".equalsIgnoreCase(status.get())) {
            task.setStatus("DONE");
            if (task.getCompletedAt() == null) {
                task.setCompletedAt(Instant.now());
            }
            shiftToNextDueDateIfAutoRenew(task);
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
        shiftToNextDueDateIfAutoRenew(task);
        status.set("DONE");
        taskService.save(task);
        loadTasks();
        selectedTask.set(task);
    }

    public void selectById(UUID id) {
        if (id == null) return;
        tasks.stream().filter(t -> t.getId().equals(id)).findFirst().ifPresent(selectedTask::set);
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
            dueDate.set(null);
            status.set("TODO");
            type.set("-");
            renewable.set("-");
            return;
        }
        title.set(task.getTitle() == null ? "" : task.getTitle());
        description.set(task.getDescription() == null ? "" : task.getDescription());
        dueDate.set(task.getDueDate());
        status.set(task.getStatus() == null ? "TODO" : task.getStatus());
        RentTaskRule rule = resolveRule(task);
        if (rule == null) {
            type.set("Ponctuelle");
            renewable.set("Non");
        } else {
            type.set(labelForFrequency(rule.frequency()));
            renewable.set(rule.autoRenew() ? "Oui" : "Non");
        }
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

    private boolean contains(String value, String q) {
        return value != null && value.toLowerCase().contains(q);
    }

    private void shiftToNextDueDateIfAutoRenew(TaskItem task) {
        RentTaskRule rule = resolveRule(task);
        if (rule == null || !rule.autoRenew()) return;
        LocalDate baseDate = task.getDueDate() == null ? LocalDate.now() : task.getDueDate();
        task.setDueDate(nextDueDate(baseDate, rule.frequency()));
    }

    private RentTaskRule resolveRule(TaskItem task) {
        if (task == null || task.getRentId() == null || task.getRentRuleId() == null) return null;
        return rentService.listRules(task.getRentId()).stream()
                .filter(rule -> rule.id().equals(task.getRentRuleId()))
                .findFirst()
                .orElse(null);
    }

    private LocalDate nextDueDate(LocalDate currentDueDate, String frequency) {
        return switch (frequency) {
            case "WEEKLY" -> currentDueDate.plusWeeks(1);
            case "MONTHLY" -> currentDueDate.plusMonths(1);
            case "QUARTERLY" -> currentDueDate.plusMonths(3);
            case "YEARLY" -> currentDueDate.plusYears(1);
            default -> currentDueDate;
        };
    }

    public UUID selectedTaskRentId() {
        TaskItem task = selectedTask.get();
        return task == null ? null : task.getRentId();
    }

    private String labelForFrequency(String code) {
        return switch (code) {
            case "WEEKLY" -> "Hebdomadaire";
            case "MONTHLY" -> "Mensuelle";
            case "QUARTERLY" -> "Trimestrielle";
            case "YEARLY" -> "Annuelle";
            default -> code;
        };
    }

}
