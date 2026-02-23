package com.imopro.application;

import com.imopro.domain.Contact;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContactRepository {
    List<Contact> findAll();

    Optional<Contact> findById(UUID id);

    void save(Contact contact);

    void delete(UUID id);
}
