package com.imopro.infra;

import com.imopro.application.PipelineRepository;
import com.imopro.domain.PipelineCard;
import com.imopro.domain.PipelineStage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SQLitePipelineRepository implements PipelineRepository {
    private final Database database;

    public SQLitePipelineRepository(Database database) {
        this.database = database;
    }

    @Override
    public List<PipelineStage> listStages() {
        String sql = "SELECT id, name, position FROM pipeline_stage ORDER BY position";
        List<PipelineStage> stages = new ArrayList<>();
        try (Connection c = database.openConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                stages.add(new PipelineStage(rs.getInt("id"), rs.getString("name"), rs.getInt("position")));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to load pipeline stages", e);
        }
        return stages;
    }

    @Override
    public Map<Integer, List<PipelineCard>> listCardsByStage() {
        List<PipelineStage> stages = listStages();
        Map<String, Integer> stageIdByName = new HashMap<>();
        Map<Integer, List<PipelineCard>> result = new HashMap<>();
        for (PipelineStage stage : stages) {
            stageIdByName.put(stage.name(), stage.id());
            result.put(stage.id(), new ArrayList<>());
        }

        String sql = "SELECT id, title, city, status FROM property ORDER BY updated_at DESC";
        try (Connection c = database.openConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                UUID propertyId = UUID.fromString(rs.getString("id"));
                String title = rs.getString("title");
                String city = rs.getString("city");
                String status = rs.getString("status");
                Integer stageId = stageIdByName.get(status);
                if (stageId != null) {
                    result.get(stageId).add(new PipelineCard(propertyId, title, city, status));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to load pipeline cards", e);
        }

        return result;
    }

    @Override
    public void movePropertyToStage(UUID propertyId, int stageId, String stageName) {
        String updateProperty = "UPDATE property SET status = ?, updated_at = ? WHERE id = ?";
        String insertEvent = "INSERT INTO pipeline_event (id, property_id, stage_id, changed_at, notes) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = database.openConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement up = c.prepareStatement(updateProperty);
                 PreparedStatement ev = c.prepareStatement(insertEvent)) {
                String now = Instant.now().toString();
                up.setString(1, stageName);
                up.setString(2, now);
                up.setString(3, propertyId.toString());
                up.executeUpdate();

                ev.setString(1, UUID.randomUUID().toString());
                ev.setString(2, propertyId.toString());
                ev.setInt(3, stageId);
                ev.setString(4, now);
                ev.setString(5, "Déplacement vers " + stageName);
                ev.executeUpdate();

                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to move pipeline card", e);
        }
    }
}
