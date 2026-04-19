package com.imopro.ui;

import com.imopro.application.ContactService;
import com.imopro.application.DocumentService;
import com.imopro.application.PropertyService;
import com.imopro.application.RentService;
import com.imopro.application.TaskService;
import com.imopro.domain.Contact;
import com.imopro.domain.DocumentItem;
import com.imopro.domain.Property;
import com.imopro.domain.Rent;
import com.imopro.domain.RentTaskRule;
import com.imopro.domain.TaskItem;
import com.imopro.infra.LocalDocumentStorage;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.nio.file.Files;
import java.nio.file.Path;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RentViewModel {
    private final RentService rentService;
    private final ContactService contactService;
    private final PropertyService propertyService;
    private final TaskService taskService;
    private final DocumentService documentService;
    private final LocalDocumentStorage storage;

    private final ObservableList<Rent> rents = FXCollections.observableArrayList();
    private final ObservableList<Contact> contacts = FXCollections.observableArrayList();
    private final ObservableList<Property> properties = FXCollections.observableArrayList();
    private final ObservableList<RentTaskRule> rules = FXCollections.observableArrayList();
    private final ObservableList<TaskItem> tasks = FXCollections.observableArrayList();
    private final ObservableList<DocumentItem> documents = FXCollections.observableArrayList();
    private final Map<UUID, RentTaskRule> ruleById = new HashMap<>();

    private final ObjectProperty<Rent> selectedRent = new SimpleObjectProperty<>();
    private final ObjectProperty<Contact> selectedContact = new SimpleObjectProperty<>();
    private final ObjectProperty<Property> selectedProperty = new SimpleObjectProperty<>();

    private final StringProperty amount = new SimpleStringProperty("");
    private final ObjectProperty<LocalDate> startDate = new SimpleObjectProperty<>(LocalDate.now());
    private final ObjectProperty<LocalDate> endDate = new SimpleObjectProperty<>();
    private final StringProperty notes = new SimpleStringProperty("");

    public RentViewModel(RentService rentService, ContactService contactService, PropertyService propertyService, TaskService taskService, DocumentService documentService, LocalDocumentStorage storage) {
        this.rentService = rentService;
        this.contactService = contactService;
        this.propertyService = propertyService;
        this.taskService = taskService;
        this.documentService = documentService;
        this.storage = storage;
        refreshAll();
        selectedRent.addListener((o, a, b) -> populateForm(b));
        selectedProperty.addListener((o, a, b) -> updateAmountFromSelectedProperty());
    }

    public void refreshAll() {
        rentService.generateDueTasks();
        contacts.setAll(contactService.listContacts());
        properties.setAll(propertyService.listProperties());
        rents.setAll(rentService.listRents());
        if (selectedRent.get() != null) {
            selectedRent.set(rents.stream().filter(r -> r.getId().equals(selectedRent.get().getId())).findFirst().orElse(null));
        }
    }

    public ObservableList<Rent> rents() { return rents; }
    public ObservableList<Contact> contacts() { return contacts; }
    public ObservableList<Property> properties() { return properties; }
    public ObservableList<RentTaskRule> rules() { return rules; }
    public ObservableList<TaskItem> tasks() { return tasks; }
    public ObservableList<DocumentItem> documents() { return documents; }
    public ObjectProperty<Rent> selectedRentProperty() { return selectedRent; }
    public ObjectProperty<Contact> selectedContactProperty() { return selectedContact; }
    public ObjectProperty<Property> selectedPropertyProperty() { return selectedProperty; }
    public StringProperty amountProperty() { return amount; }
    public ObjectProperty<LocalDate> startDateProperty() { return startDate; }
    public ObjectProperty<LocalDate> endDateProperty() { return endDate; }
    public StringProperty notesProperty() { return notes; }

    public void createRent() {
        Rent r = Rent.create();
        rents.add(0, r);
        selectedRent.set(r);
    }

    public void selectById(UUID id) {
        if (id == null) return;
        rents.stream().filter(r -> r.getId().equals(id)).findFirst().ifPresent(selectedRent::set);
    }

    public void saveRent() {
        Rent r = selectedRent.get();
        if (r == null || selectedContact.get() == null || selectedProperty.get() == null) return;
        BigDecimal parsedAmount = parse(amount.get());
        r.setContactId(selectedContact.get().getId());
        r.setPropertyId(selectedProperty.get().getId());
        r.setMonthlyAmount(parsedAmount);
        r.setStartDate(startDate.get());
        r.setEndDate(endDate.get());
        r.setNotes(notes.get());
        rentService.save(r);

        Property p = selectedProperty.get();
        p.setPrice(parsedAmount);
        p.touchUpdatedAt();
        propertyService.save(p);

        refreshAll();
        selectedRent.set(rents.stream().filter(it -> it.getId().equals(r.getId())).findFirst().orElse(r));
    }

    public void deleteRent() {
        Rent r = selectedRent.get();
        if (r == null) return;
        rentService.delete(r.getId());
        rents.remove(r);
        selectedRent.set(null);
        rules.clear(); tasks.clear(); documents.clear();
    }

    public void addRule(String frequencyCode, boolean autoRenew, Integer dayOfWeek, Integer dayOfMonth, Integer monthOfYear) {
        if (selectedRent.get() == null) return;
        if (selectedContact.get() == null || selectedProperty.get() == null) return;

        // Ensure rent exists in DB before linking rule/task rows to it.
        saveRent();
        Rent r = selectedRent.get();
        if (r == null) return;
        String propertyTitle = selectedProperty.get().displayTitle();
        String contactFullName = selectedContact.get().displayName();
        String taskTitle = propertyTitle + " - " + contactFullName;
        String taskDescription = "Tâche générée automatiquement pour le loyer " + propertyTitle
                + " du locataire " + contactFullName;
        RentTaskRule rule = new RentTaskRule(
                UUID.randomUUID(), r.getId(), frequencyCode, autoRenew,
                dayOfWeek, dayOfMonth, monthOfYear,
                taskTitle,
                taskDescription,
                null, true);
        UUID savedRuleId = null;
        try {
            rentService.saveRule(rule);
            savedRuleId = rule.id();
        } catch (RuntimeException ignored) {
            // Do not block linked-task creation if rule persistence fails.
        }

        TaskItem linkedTask = TaskItem.newTask();
        linkedTask.setTitle(taskTitle);
        linkedTask.setDescription(taskDescription);
        linkedTask.setDueDate(LocalDate.now());
        linkedTask.setRentId(r.getId());
        linkedTask.setRentRuleId(savedRuleId);
        taskService.save(linkedTask);

        loadLinked(r);
    }

    public void removeRule(RentTaskRule rule) {
        if (rule == null) return;
        rentService.deleteRule(rule.id());
        Rent r = selectedRent.get();
        if (r != null) loadLinked(r);
    }

    public String rentDisplay(Rent r) {
        return "Loyer " + shortId(r.getId()) + " - " + (r.getMonthlyAmount() == null ? "?" : r.getMonthlyAmount());
    }

    public String ruleDisplay(RentTaskRule r) {
        return labelForFrequency(r.frequency()) + " | auto=" + (r.autoRenew() ? "oui" : "non")
                + " | jS=" + (r.dayOfWeek() == null ? "-" : r.dayOfWeek())
                + " | jM=" + (r.dayOfMonth() == null ? "-" : r.dayOfMonth())
                + " | m=" + (r.monthOfYear() == null ? "-" : r.monthOfYear());
    }

    private void populateForm(Rent r) {
        if (r == null) {
            selectedContact.set(null); selectedProperty.set(null); amount.set(""); startDate.set(LocalDate.now()); endDate.set(null); notes.set("");
            rules.clear(); tasks.clear(); documents.clear();
            return;
        }
        selectedContact.set(contacts.stream().filter(c -> c.getId().equals(r.getContactId())).findFirst().orElse(null));
        selectedProperty.set(properties.stream().filter(p -> p.getId().equals(r.getPropertyId())).findFirst().orElse(null));
        updateAmountFromSelectedProperty();
        startDate.set(r.getStartDate());
        endDate.set(r.getEndDate());
        notes.set(r.getNotes() == null ? "" : r.getNotes());
        loadLinked(r);
    }

    private void loadLinked(Rent r) {
        List<RentTaskRule> loadedRules = rentService.listRules(r.getId());
        rules.setAll(loadedRules);
        ruleById.clear();
        loadedRules.forEach(rule -> ruleById.put(rule.id(), rule));
        List<TaskItem> allTasks = taskService.listTasks();
        tasks.setAll(allTasks.stream().filter(t -> r.getId().equals(t.getRentId())).toList());
        List<DocumentItem> allDocs = documentService.listDocuments();
        documents.setAll(allDocs.stream().filter(d -> r.getId().equals(d.getRentId())).toList());
    }

    public String taskType(TaskItem task) {
        RentTaskRule rule = resolveRule(task);
        if (rule == null) return "Ponctuelle";
        return labelForFrequency(rule.frequency());
    }

    public String taskDueDateDisplay(TaskItem task) {
        return task.getDueDate() == null ? "-" : task.getDueDate().toString();
    }

    public String taskRenewableIcon(TaskItem task) {
        RentTaskRule rule = resolveRule(task);
        return rule != null && rule.autoRenew() ? "✔" : "✖";
    }

    public UUID selectedRentId() {
        Rent r = selectedRent.get();
        return r == null ? null : r.getId();
    }

    public void importDocumentForSelectedRent(Path sourcePath) {
        UUID rentId = selectedRentId();
        if (sourcePath == null || rentId == null) return;
        try {
            Path rel = storage.importFile(sourcePath);
            DocumentItem item = DocumentItem.newDocument();
            item.setFileName(sourcePath.getFileName().toString());
            item.setFilePath(rel.toString());
            String detected = Files.probeContentType(sourcePath);
            item.setMimeType(detected == null ? "application/octet-stream" : detected);
            item.setSizeBytes(Files.size(sourcePath));
            item.setRentId(rentId);
            documentService.save(item);
            Rent r = selectedRent.get();
            if (r != null) loadLinked(r);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to import document for rent", e);
        }
    }

    public void openLinkedDocument(DocumentItem item) {
        if (item == null || item.getFilePath() == null || item.getFilePath().isBlank()) return;
        storage.open(item.getFilePath());
    }

    public String documentAddedDate(DocumentItem item) {
        if (item == null || item.getCreatedAt() == null) return "-";
        return DateTimeFormatter.ofPattern("yyyy-MM-dd")
                .format(item.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate());
    }

    public boolean isTaskOverdueTodo(TaskItem task) {
        return task != null
                && task.getDueDate() != null
                && task.getDueDate().isBefore(LocalDate.now())
                && !"DONE".equals(task.getStatus());
    }

    public boolean isTaskDoneAndNonRenewable(TaskItem task) {
        return task != null && "DONE".equals(task.getStatus()) && !isTaskRenewable(task);
    }

    private boolean isTaskRenewable(TaskItem task) {
        RentTaskRule rule = resolveRule(task);
        return rule != null && rule.autoRenew();
    }

    private RentTaskRule resolveRule(TaskItem task) {
        if (task == null || task.getRentRuleId() == null) return null;
        return ruleById.get(task.getRentRuleId());
    }

    private void updateAmountFromSelectedProperty() {
        Property p = selectedProperty.get();
        if (p == null || p.getPrice() == null) {
            amount.set("");
            return;
        }
        amount.set(p.getPrice().toPlainString());
    }

    private BigDecimal parse(String v) { try { return (v == null || v.isBlank()) ? null : new BigDecimal(v); } catch (Exception e) { return null; } }
    private String shortId(UUID id) { return id.toString().substring(0, 8); }

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
