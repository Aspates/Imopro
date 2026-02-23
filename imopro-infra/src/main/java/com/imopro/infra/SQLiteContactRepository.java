package com.imopro.infra;

import com.imopro.application.ContactRepository;
import com.imopro.domain.Contact;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SQLiteContactRepository implements ContactRepository {
    private final Database database;

    public SQLiteContactRepository(Database database) {
        this.database = database;
    }

    @Override
    public List<Contact> findAll() {
        String sql = """
                SELECT id, first_name, last_name, phone, email, address, notes, created_at, updated_at
                FROM contact
                ORDER BY last_name, first_name
                """;
        List<Contact> results = new ArrayList<>();
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                results.add(mapContact(resultSet));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to load contacts", e);
        }
        return results;
    }

    @Override
    public Optional<Contact> findById(UUID id) {
        String sql = """
                SELECT id, first_name, last_name, phone, email, address, notes, created_at, updated_at
                FROM contact
                WHERE id = ?
                """;
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapContact(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to load contact", e);
        }
        return Optional.empty();
    }

    @Override
    public void save(Contact contact) {
        String sql = """
                INSERT INTO contact (id, first_name, last_name, phone, email, address, notes, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(id) DO UPDATE SET
                    first_name = excluded.first_name,
                    last_name = excluded.last_name,
                    phone = excluded.phone,
                    email = excluded.email,
                    address = excluded.address,
                    notes = excluded.notes,
                    updated_at = excluded.updated_at
                """;
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, contact.getId().toString());
            statement.setString(2, contact.getFirstName());
            statement.setString(3, contact.getLastName());
            statement.setString(4, contact.getPhone());
            statement.setString(5, contact.getEmail());
            statement.setString(6, contact.getAddress());
            statement.setString(7, contact.getNotes());
            statement.setString(8, contact.getCreatedAt().toString());
            statement.setString(9, contact.getUpdatedAt().toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to save contact", e);
        }
    }

    @Override
    public void delete(UUID id) {
        String sql = "DELETE FROM contact WHERE id = ?";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to delete contact", e);
        }
    }

    private Contact mapContact(ResultSet resultSet) throws SQLException {
        UUID id = UUID.fromString(resultSet.getString("id"));
        String firstName = resultSet.getString("first_name");
        String lastName = resultSet.getString("last_name");
        String phone = resultSet.getString("phone");
        String email = resultSet.getString("email");
        String address = resultSet.getString("address");
        String notes = resultSet.getString("notes");
        Instant createdAt = Instant.parse(resultSet.getString("created_at"));
        Instant updatedAt = Instant.parse(resultSet.getString("updated_at"));
        return new Contact(id, firstName, lastName, phone, email, address, notes, createdAt, updatedAt);
    }
}
