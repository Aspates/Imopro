package com.imopro.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Property {
    private final UUID id;
    private String title;
    private String address;
    private String city;
    private String postalCode;
    private String propertyType;
    private Double surface;
    private Integer rooms;
    private BigDecimal price;
    private String status;
    private UUID ownerContactId;
    private final Instant createdAt;
    private Instant updatedAt;

    public Property(UUID id,
                    String title,
                    String address,
                    String city,
                    String postalCode,
                    String propertyType,
                    Double surface,
                    Integer rooms,
                    BigDecimal price,
                    String status,
                    UUID ownerContactId,
                    Instant createdAt,
                    Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.title = title;
        this.address = address;
        this.city = city;
        this.postalCode = postalCode;
        this.propertyType = propertyType;
        this.surface = surface;
        this.rooms = rooms;
        this.price = price;
        this.status = status;
        this.ownerContactId = ownerContactId;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }

    public static Property newProperty() {
        Instant now = Instant.now();
        return new Property(UUID.randomUUID(), "", "", "", "", "", null, null, null, "Lead", null, now, now);
    }

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    public String getPropertyType() { return propertyType; }
    public void setPropertyType(String propertyType) { this.propertyType = propertyType; }
    public Double getSurface() { return surface; }
    public void setSurface(Double surface) { this.surface = surface; }
    public Integer getRooms() { return rooms; }
    public void setRooms(Integer rooms) { this.rooms = rooms; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public UUID getOwnerContactId() { return ownerContactId; }
    public void setOwnerContactId(UUID ownerContactId) { this.ownerContactId = ownerContactId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void touchUpdatedAt() { this.updatedAt = Instant.now(); }

    public String displayTitle() {
        return (title == null || title.isBlank()) ? "(Sans titre)" : title;
    }
}
