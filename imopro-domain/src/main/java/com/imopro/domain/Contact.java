package com.imopro.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Contact {
    private final UUID id;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private String address;
    private String notes;
    private final Instant createdAt;
    private Instant updatedAt;

    public Contact(UUID id,
                   String firstName,
                   String lastName,
                   String phone,
                   String email,
                   String address,
                   String notes,
                   Instant createdAt,
                   Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.notes = notes;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }

    public static Contact newContact() {
        Instant now = Instant.now();
        return new Contact(UUID.randomUUID(), "", "", "", "", "", "", now, now);
    }

    public UUID getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void touchUpdatedAt() {
        this.updatedAt = Instant.now();
    }

    public String displayName() {
        String full = (firstName + " " + lastName).trim();
        return full.isEmpty() ? "(Sans nom)" : full;
    }
}
