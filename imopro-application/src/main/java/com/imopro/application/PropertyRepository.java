package com.imopro.application;

import com.imopro.domain.Property;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PropertyRepository {
    List<Property> findAll();

    Optional<Property> findById(UUID id);

    void save(Property property);

    void delete(UUID id);
}
