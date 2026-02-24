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
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class RentViewModel {
    private final RentService rentService;
    private final ContactService contactService;
    private final PropertyService propertyService;
    private final TaskService taskService;
    private final DocumentService documentService;

    private final ObservableList<Rent> rents = FXCollections.observableArrayList();
    private final ObservableList<Contact> contacts = FXCollections.observableArrayList();
    private final ObservableList<Property> properties = FXCollections.observableArrayList();
    private final ObservableList<RentTaskRule> rules = FXCollections.observableArrayList();
    private final ObservableList<TaskItem> tasks = FXCollections.observableArrayList();
    private final ObservableList<DocumentItem> documents = FXCollections.observableArrayList();

    private final ObjectProperty<Rent> selectedRent = new SimpleObjectProperty<>();
    private final ObjectProperty<Contact> selectedContact = new SimpleObjectProperty<>();
    private final ObjectProperty<Property> selectedProperty = new SimpleObjectProperty<>();

    private final StringProperty amount = new SimpleStringProperty("");
    private final ObjectProperty<LocalDate> startDate = new SimpleObjectProperty<>(LocalDate.now());
    private final ObjectProperty<LocalDate> endDate = new SimpleObjectProperty<>();
    private final StringProperty notes = new SimpleStringProperty("");

    public RentViewModel(RentService rentService, ContactService contactService, PropertyService propertyService, TaskService taskService, DocumentService documentService) {
        this.rentService = rentService;
        this.contactService = contactService;
        this.propertyService = propertyService;
        this.taskService = taskService;
        this.documentService = documentService;
        refreshAll();
        selectedRent.addListener((o, a, b) -> populateForm(b));
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

    public void saveRent() {
        Rent r = selectedRent.get();
        if (r == null || selectedContact.get() == null || selectedProperty.get() == null) return;
        r.setContactId(selectedContact.get().getId());
        r.setPropertyId(selectedProperty.get().getId());
        r.setMonthlyAmount(parse(amount.get()));
        r.setStartDate(startDate.get());
        r.setEndDate(endDate.get());
        r.setNotes(notes.get());
        rentService.save(r);
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
        Rent r = selectedRent.get();
        if (r == null) return;
        RentTaskRule rule = new RentTaskRule(
                UUID.randomUUID(), r.getId(), frequencyCode, autoRenew,
                dayOfWeek, dayOfMonth, monthOfYear,
                "Loyer " + shortId(r.getId()) + " - " + labelForFrequency(frequencyCode),
                "Tâche générée automatiquement pour le loyer " + shortId(r.getId()),
                null, true);
        rentService.saveRule(rule);
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
        amount.set(r.getMonthlyAmount() == null ? "" : r.getMonthlyAmount().toPlainString());
        startDate.set(r.getStartDate());
        endDate.set(r.getEndDate());
        notes.set(r.getNotes() == null ? "" : r.getNotes());
        loadLinked(r);
    }

    private void loadLinked(Rent r) {
        rules.setAll(rentService.listRules(r.getId()));
        List<TaskItem> allTasks = taskService.listTasks();
        tasks.setAll(allTasks.stream().filter(t -> r.getId().equals(t.getRentId())).toList());
        List<DocumentItem> allDocs = documentService.listDocuments();
        documents.setAll(allDocs.stream().filter(d -> r.getId().equals(d.getRentId())).toList());
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
