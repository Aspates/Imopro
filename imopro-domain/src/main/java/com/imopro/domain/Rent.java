package com.imopro.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class Rent {
    private final UUID id;
    private UUID contactId;
    private UUID propertyId;
    private BigDecimal monthlyAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private String notes;

    public Rent(UUID id, UUID contactId, UUID propertyId, BigDecimal monthlyAmount, LocalDate startDate, LocalDate endDate, String notes) {
        this.id = id;
        this.contactId = contactId;
        this.propertyId = propertyId;
        this.monthlyAmount = monthlyAmount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.notes = notes;
    }

    public static Rent create() {
        return new Rent(UUID.randomUUID(), null, null, null, LocalDate.now(), null, "");
    }

    public UUID getId() { return id; }
    public UUID getContactId() { return contactId; }
    public void setContactId(UUID contactId) { this.contactId = contactId; }
    public UUID getPropertyId() { return propertyId; }
    public void setPropertyId(UUID propertyId) { this.propertyId = propertyId; }
    public BigDecimal getMonthlyAmount() { return monthlyAmount; }
    public void setMonthlyAmount(BigDecimal monthlyAmount) { this.monthlyAmount = monthlyAmount; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
