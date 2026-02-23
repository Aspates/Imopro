package com.imopro.infra;

import com.imopro.application.PropertyRepository;
import com.imopro.domain.Property;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SQLitePropertyRepository implements PropertyRepository {
    private final Database database;

    public SQLitePropertyRepository(Database database) {
        this.database = database;
    }

    @Override
    public List<Property> findAll() {
        String sql = """
                SELECT id, title, address, city, postal_code, property_type, surface, rooms, price, status,
                       owner_contact_id, created_at, updated_at
                FROM property
                ORDER BY updated_at DESC
                """;
        List<Property> results = new ArrayList<>();
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                results.add(mapProperty(resultSet));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to load properties", e);
        }
        return results;
    }

    @Override
    public Optional<Property> findById(UUID id) {
        String sql = """
                SELECT id, title, address, city, postal_code, property_type, surface, rooms, price, status,
                       owner_contact_id, created_at, updated_at
                FROM property
                WHERE id = ?
                """;
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapProperty(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to load property", e);
        }
        return Optional.empty();
    }

    @Override
    public void save(Property property) {
        String sql = """
                INSERT INTO property (id, title, address, city, postal_code, property_type, surface, rooms, price,
                                      status, owner_contact_id, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(id) DO UPDATE SET
                    title = excluded.title,
                    address = excluded.address,
                    city = excluded.city,
                    postal_code = excluded.postal_code,
                    property_type = excluded.property_type,
                    surface = excluded.surface,
                    rooms = excluded.rooms,
                    price = excluded.price,
                    status = excluded.status,
                    owner_contact_id = excluded.owner_contact_id,
                    updated_at = excluded.updated_at
                """;
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, property.getId().toString());
            statement.setString(2, property.getTitle());
            statement.setString(3, property.getAddress());
            statement.setString(4, property.getCity());
            statement.setString(5, property.getPostalCode());
            statement.setString(6, property.getPropertyType());
            if (property.getSurface() == null) statement.setNull(7, java.sql.Types.DOUBLE); else statement.setDouble(7, property.getSurface());
            if (property.getRooms() == null) statement.setNull(8, java.sql.Types.INTEGER); else statement.setInt(8, property.getRooms());
            if (property.getPrice() == null) statement.setNull(9, java.sql.Types.DECIMAL); else statement.setBigDecimal(9, property.getPrice());
            statement.setString(10, property.getStatus());
            if (property.getOwnerContactId() == null) statement.setNull(11, java.sql.Types.VARCHAR); else statement.setString(11, property.getOwnerContactId().toString());
            statement.setString(12, property.getCreatedAt().toString());
            statement.setString(13, property.getUpdatedAt().toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to save property", e);
        }
    }

    @Override
    public void delete(UUID id) {
        String sql = "DELETE FROM property WHERE id = ?";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to delete property", e);
        }
    }

    private Property mapProperty(ResultSet rs) throws SQLException {
        String ownerRaw = rs.getString("owner_contact_id");
        return new Property(
                UUID.fromString(rs.getString("id")),
                rs.getString("title"),
                rs.getString("address"),
                rs.getString("city"),
                rs.getString("postal_code"),
                rs.getString("property_type"),
                (Double) rs.getObject("surface"),
                (Integer) rs.getObject("rooms"),
                rs.getBigDecimal("price") == null ? null : new BigDecimal(rs.getBigDecimal("price").toPlainString()),
                rs.getString("status"),
                ownerRaw == null ? null : UUID.fromString(ownerRaw),
                Instant.parse(rs.getString("created_at")),
                Instant.parse(rs.getString("updated_at"))
        );
    }
}
