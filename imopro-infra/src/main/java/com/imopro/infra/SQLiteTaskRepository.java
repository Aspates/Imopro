package com.imopro.infra;

import com.imopro.application.TaskRepository;
import com.imopro.domain.TaskItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SQLiteTaskRepository implements TaskRepository {
    private final Database database;

    public SQLiteTaskRepository(Database database) {
        this.database = database;
    }

    @Override
    public List<TaskItem> findAll() {
        String sql = """
                SELECT id, title, description, status, due_date, created_at, completed_at
                FROM task
                ORDER BY CASE WHEN due_date IS NULL THEN 1 ELSE 0 END,
                         due_date ASC,
                         created_at DESC
                """;
        List<TaskItem> tasks = new ArrayList<>();
        try (Connection c = database.openConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                tasks.add(mapTask(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to load tasks", e);
        }
        return tasks;
    }

    @Override
    public Optional<TaskItem> findById(UUID id) {
        String sql = """
                SELECT id, title, description, status, due_date, created_at, completed_at
                FROM task
                WHERE id = ?
                """;
        try (Connection c = database.openConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapTask(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to load task", e);
        }
        return Optional.empty();
    }

    @Override
    public void save(TaskItem task) {
        String sql = """
                INSERT INTO task (id, title, description, status, due_date, created_at, completed_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(id) DO UPDATE SET
                    title = excluded.title,
                    description = excluded.description,
                    status = excluded.status,
                    due_date = excluded.due_date,
                    completed_at = excluded.completed_at
                """;
        try (Connection c = database.openConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, task.getId().toString());
            ps.setString(2, task.getTitle());
            ps.setString(3, task.getDescription());
            ps.setString(4, task.getStatus());
            if (task.getDueDate() == null) ps.setNull(5, java.sql.Types.VARCHAR); else ps.setString(5, task.getDueDate().toString());
            ps.setString(6, task.getCreatedAt().toString());
            if (task.getCompletedAt() == null) ps.setNull(7, java.sql.Types.VARCHAR); else ps.setString(7, task.getCompletedAt().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to save task", e);
        }
    }

    @Override
    public void delete(UUID id) {
        String sql = "DELETE FROM task WHERE id = ?";
        try (Connection c = database.openConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to delete task", e);
        }
    }

    private TaskItem mapTask(ResultSet rs) throws SQLException {
        String due = rs.getString("due_date");
        String completed = rs.getString("completed_at");
        return new TaskItem(
                UUID.fromString(rs.getString("id")),
                rs.getString("title"),
                rs.getString("description"),
                rs.getString("status"),
                due == null ? null : LocalDate.parse(due),
                Instant.parse(rs.getString("created_at")),
                completed == null ? null : Instant.parse(completed)
        );
    }
}
