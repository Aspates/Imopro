package com.imopro.infra;

import com.imopro.application.DocumentRepository;
import com.imopro.domain.DocumentItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SQLiteDocumentRepository implements DocumentRepository {
    private final Database database;

    public SQLiteDocumentRepository(Database database) {
        this.database = database;
    }

    @Override
    public List<DocumentItem> findAll() {
        String sql = """
                SELECT id, file_name, file_path, mime_type, size_bytes, contact_id, property_id, rent_id, created_at
                FROM document
                ORDER BY created_at DESC
                """;
        List<DocumentItem> docs = new ArrayList<>();
        try (Connection c = database.openConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                docs.add(mapDocument(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to load documents", e);
        }
        return docs;
    }

    @Override
    public Optional<DocumentItem> findById(UUID id) {
        String sql = """
                SELECT id, file_name, file_path, mime_type, size_bytes, contact_id, property_id, rent_id, created_at
                FROM document
                WHERE id = ?
                """;
        try (Connection c = database.openConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapDocument(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to load document", e);
        }
        return Optional.empty();
    }

    @Override
    public void save(DocumentItem document) {
        String sql = """
                INSERT INTO document (id, file_name, file_path, mime_type, size_bytes, contact_id, property_id, rent_id, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(id) DO UPDATE SET
                    file_name = excluded.file_name,
                    file_path = excluded.file_path,
                    mime_type = excluded.mime_type,
                    size_bytes = excluded.size_bytes,
                    contact_id = excluded.contact_id,
                    property_id = excluded.property_id,
                    rent_id = excluded.rent_id
                """;
        try (Connection c = database.openConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, document.getId().toString());
            ps.setString(2, document.getFileName());
            ps.setString(3, document.getFilePath());
            ps.setString(4, document.getMimeType());
            if (document.getSizeBytes() == null) ps.setNull(5, java.sql.Types.BIGINT); else ps.setLong(5, document.getSizeBytes());
            if (document.getContactId() == null) ps.setNull(6, java.sql.Types.VARCHAR); else ps.setString(6, document.getContactId().toString());
            if (document.getPropertyId() == null) ps.setNull(7, java.sql.Types.VARCHAR); else ps.setString(7, document.getPropertyId().toString());
            if (document.getRentId() == null) ps.setNull(8, java.sql.Types.VARCHAR); else ps.setString(8, document.getRentId().toString());
            ps.setString(9, document.getCreatedAt().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to save document", e);
        }
    }

    @Override
    public void delete(UUID id) {
        String sql = "DELETE FROM document WHERE id = ?";
        try (Connection c = database.openConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to delete document", e);
        }
    }

    private DocumentItem mapDocument(ResultSet rs) throws SQLException {
        String contactRaw = rs.getString("contact_id");
        String propertyRaw = rs.getString("property_id");
        String rentRaw = rs.getString("rent_id");
        return new DocumentItem(
                UUID.fromString(rs.getString("id")),
                rs.getString("file_name"),
                rs.getString("file_path"),
                rs.getString("mime_type"),
                rs.getObject("size_bytes") == null ? null : rs.getLong("size_bytes"),
                contactRaw == null ? null : UUID.fromString(contactRaw),
                propertyRaw == null ? null : UUID.fromString(propertyRaw),
                rentRaw == null ? null : UUID.fromString(rentRaw),
                Instant.parse(rs.getString("created_at"))
        );
    }
}
