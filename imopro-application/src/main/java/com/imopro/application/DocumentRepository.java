package com.imopro.application;

import com.imopro.domain.DocumentItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository {
    List<DocumentItem> findAll();

    Optional<DocumentItem> findById(UUID id);

    void save(DocumentItem document);

    void delete(UUID id);
}
