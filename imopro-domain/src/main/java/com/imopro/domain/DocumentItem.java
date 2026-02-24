package com.imopro.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class DocumentItem {
    private final UUID id;
    private String fileName;
    private String filePath;
    private String mimeType;
    private Long sizeBytes;
    private UUID contactId;
    private UUID propertyId;
    private final Instant createdAt;

    public DocumentItem(UUID id,
                        String fileName,
                        String filePath,
                        String mimeType,
                        Long sizeBytes,
                        UUID contactId,
                        UUID propertyId,
                        Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.fileName = fileName;
        this.filePath = filePath;
        this.mimeType = mimeType;
        this.sizeBytes = sizeBytes;
        this.contactId = contactId;
        this.propertyId = propertyId;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
    }

    public static DocumentItem newDocument() {
        return new DocumentItem(UUID.randomUUID(), "", "", "application/octet-stream", 0L, null, null, Instant.now());
    }

    public UUID getId() { return id; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public Long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(Long sizeBytes) { this.sizeBytes = sizeBytes; }
    public UUID getContactId() { return contactId; }
    public void setContactId(UUID contactId) { this.contactId = contactId; }
    public UUID getPropertyId() { return propertyId; }
    public void setPropertyId(UUID propertyId) { this.propertyId = propertyId; }
    public Instant getCreatedAt() { return createdAt; }

    public String displayName() {
        return (fileName == null || fileName.isBlank()) ? "(Sans nom)" : fileName;
    }
}
