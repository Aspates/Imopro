package com.imopro.application;

import com.imopro.domain.Contact;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ContactService {
    private final ContactRepository repository;

    public ContactService(ContactRepository repository) {
        this.repository = repository;
    }

    public List<Contact> listContacts() {
        return repository.findAll();
    }

    public Optional<Contact> getContact(UUID id) {
        return repository.findById(id);
    }

    public void save(Contact contact) {
        repository.save(contact);
    }

    public void delete(UUID id) {
        repository.delete(id);
    }
}
