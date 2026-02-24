package com.imopro.application;

import com.imopro.domain.DocumentItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DocumentService {
    private final DocumentRepository repository;

    public DocumentService(DocumentRepository repository) {
        this.repository = repository;
    }

    public List<DocumentItem> listDocuments() {
        return repository.findAll();
    }

    public Optional<DocumentItem> getDocument(UUID id) {
        return repository.findById(id);
    }

    public void save(DocumentItem document) {
        repository.save(document);
    }

    public void delete(UUID id) {
        repository.delete(id);
    }
}
