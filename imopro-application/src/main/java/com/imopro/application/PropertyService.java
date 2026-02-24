package com.imopro.application;

import com.imopro.domain.Property;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PropertyService {
    private final PropertyRepository repository;

    public PropertyService(PropertyRepository repository) {
        this.repository = repository;
    }

    public List<Property> listProperties() {
        return repository.findAll();
    }

    public Optional<Property> getProperty(UUID id) {
        return repository.findById(id);
    }

    public void save(Property property) {
        repository.save(property);
    }

    public void delete(UUID id) {
        repository.delete(id);
    }
}
