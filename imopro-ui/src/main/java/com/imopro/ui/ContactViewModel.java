package com.imopro.ui;

import com.imopro.application.ContactService;
import com.imopro.domain.Contact;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class ContactViewModel {
    private final ContactService contactService;
    private final ObservableList<Contact> contacts = FXCollections.observableArrayList();
    private final FilteredList<Contact> filteredContacts = new FilteredList<>(contacts, contact -> true);
    private final ObjectProperty<Contact> selectedContact = new SimpleObjectProperty<>();
    private final StringProperty searchQuery = new SimpleStringProperty("");

    private final StringProperty firstName = new SimpleStringProperty("");
    private final StringProperty lastName = new SimpleStringProperty("");
    private final StringProperty phone = new SimpleStringProperty("");
    private final StringProperty email = new SimpleStringProperty("");
    private final StringProperty address = new SimpleStringProperty("");
    private final StringProperty notes = new SimpleStringProperty("");

    public ContactViewModel(ContactService contactService) {
        this.contactService = contactService;
        loadContacts();
        selectedContact.addListener((obs, oldContact, newContact) -> populateFields(newContact));
        searchQuery.addListener((obs, oldValue, newValue) -> applyFilter(newValue));
    }

    public ObservableList<Contact> getContacts() {
        return filteredContacts;
    }

    public ObjectProperty<Contact> selectedContactProperty() {
        return selectedContact;
    }

    public StringProperty searchQueryProperty() {
        return searchQuery;
    }

    public StringProperty firstNameProperty() {
        return firstName;
    }

    public StringProperty lastNameProperty() {
        return lastName;
    }

    public StringProperty phoneProperty() {
        return phone;
    }

    public StringProperty emailProperty() {
        return email;
    }

    public StringProperty addressProperty() {
        return address;
    }

    public StringProperty notesProperty() {
        return notes;
    }

    public boolean hasSelection() {
        return selectedContact.get() != null;
    }

    public void loadContacts() {
        List<Contact> items = contactService.listContacts();
        items.sort(Comparator.comparing(Contact::getLastName).thenComparing(Contact::getFirstName));
        contacts.setAll(items);
    }

    public void createContact() {
        Contact contact = Contact.newContact();
        contacts.add(contact);
        selectedContact.set(contact);
    }

    public void saveContact() {
        Contact contact = selectedContact.get();
        if (contact == null) {
            return;
        }
        contact.setFirstName(firstName.get());
        contact.setLastName(lastName.get());
        contact.setPhone(phone.get());
        contact.setEmail(email.get());
        contact.setAddress(address.get());
        contact.setNotes(notes.get());
        contact.touchUpdatedAt();
        contactService.save(contact);
        refreshOrdering();
    }

    public void selectById(UUID id) {
        if (id == null) return;
        contacts.stream().filter(c -> c.getId().equals(id)).findFirst().ifPresent(selectedContact::set);
    }

    public void deleteSelectedContact() {
        Contact contact = selectedContact.get();
        if (contact == null) {
            return;
        }
        contactService.delete(contact.getId());
        contacts.remove(contact);
        selectedContact.set(null);
    }

    public String contactDisplayName(Contact contact) {
        return contact.displayName();
    }

    public javafx.beans.binding.BooleanBinding canSaveBinding() {
        return Bindings.createBooleanBinding(this::hasSelection, selectedContact);
    }

    public javafx.beans.binding.BooleanBinding canDeleteBinding() {
        return Bindings.createBooleanBinding(this::hasSelection, selectedContact);
    }

    private void populateFields(Contact contact) {
        if (contact == null) {
            firstName.set("");
            lastName.set("");
            phone.set("");
            email.set("");
            address.set("");
            notes.set("");
            return;
        }
        firstName.set(contact.getFirstName());
        lastName.set(contact.getLastName());
        phone.set(contact.getPhone());
        email.set(contact.getEmail());
        address.set(contact.getAddress());
        notes.set(contact.getNotes());
    }

    private void applyFilter(String query) {
        String normalized = query == null ? "" : query.toLowerCase();
        filteredContacts.setPredicate(contact -> {
            if (normalized.isBlank()) {
                return true;
            }
            return contact.displayName().toLowerCase().contains(normalized)
                    || safeContains(contact.getEmail(), normalized)
                    || safeContains(contact.getPhone(), normalized);
        });
    }

    private boolean safeContains(String value, String needle) {
        return value != null && value.toLowerCase().contains(needle);
    }

    private void refreshOrdering() {
        contacts.sort(Comparator.comparing(Contact::getLastName).thenComparing(Contact::getFirstName));
    }
}
