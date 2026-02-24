package com.imopro.infra;

import com.imopro.application.RentRepository;
import com.imopro.domain.Rent;
import com.imopro.domain.RentTaskRule;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SQLiteRentRepository implements RentRepository {
    private final Database database;

    public SQLiteRentRepository(Database database) { this.database = database; }

    @Override
    public List<Rent> findAll() {
        String sql = "SELECT id, contact_id, property_id, monthly_amount, start_date, end_date, notes FROM rent ORDER BY start_date DESC";
        List<Rent> rents = new ArrayList<>();
        try (Connection c = database.openConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rents.add(new Rent(
                        UUID.fromString(rs.getString("id")),
                        UUID.fromString(rs.getString("contact_id")),
                        UUID.fromString(rs.getString("property_id")),
                        rs.getObject("monthly_amount") == null ? null : BigDecimal.valueOf(rs.getDouble("monthly_amount")),
                        LocalDate.parse(rs.getString("start_date")),
                        rs.getString("end_date") == null ? null : LocalDate.parse(rs.getString("end_date")),
                        rs.getString("notes")
                ));
            }
        } catch (SQLException e) { throw new IllegalStateException(e); }
        return rents;
    }

    @Override
    public void save(Rent rent) {
        String sql = """
                INSERT INTO rent(id, contact_id, property_id, monthly_amount, start_date, end_date, notes)
                VALUES(?,?,?,?,?,?,?)
                ON CONFLICT(id) DO UPDATE SET
                contact_id=excluded.contact_id,
                property_id=excluded.property_id,
                monthly_amount=excluded.monthly_amount,
                start_date=excluded.start_date,
                end_date=excluded.end_date,
                notes=excluded.notes
                """;
        try (Connection c = database.openConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, rent.getId().toString());
            ps.setString(2, rent.getContactId().toString());
            ps.setString(3, rent.getPropertyId().toString());
            if (rent.getMonthlyAmount() == null) ps.setNull(4, java.sql.Types.REAL); else ps.setDouble(4, rent.getMonthlyAmount().doubleValue());
            ps.setString(5, rent.getStartDate().toString());
            if (rent.getEndDate() == null) ps.setNull(6, java.sql.Types.VARCHAR); else ps.setString(6, rent.getEndDate().toString());
            ps.setString(7, rent.getNotes());
            ps.executeUpdate();
        } catch (SQLException e) { throw new IllegalStateException(e); }
    }

    @Override
    public void delete(UUID id) {
        try (Connection c = database.openConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM rent WHERE id=?")) {
            ps.setString(1, id.toString());
            ps.executeUpdate();
        } catch (SQLException e) { throw new IllegalStateException(e); }
    }

    @Override
    public List<RentTaskRule> findRulesByRent(UUID rentId) {
        String sql = "SELECT * FROM rent_task_rule WHERE rent_id=? ORDER BY frequency";
        List<RentTaskRule> rules = new ArrayList<>();
        try (Connection c = database.openConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, rentId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rules.add(new RentTaskRule(
                            UUID.fromString(rs.getString("id")),
                            UUID.fromString(rs.getString("rent_id")),
                            rs.getString("frequency"),
                            rs.getInt("auto_renew") == 1,
                            (Integer) rs.getObject("day_of_week"),
                            (Integer) rs.getObject("day_of_month"),
                            (Integer) rs.getObject("month_of_year"),
                            rs.getString("title_prefix"),
                            rs.getString("description_prefix"),
                            rs.getString("last_generated_at") == null ? null : LocalDate.parse(rs.getString("last_generated_at")),
                            rs.getInt("active") == 1
                    ));
                }
            }
        } catch (SQLException e) { throw new IllegalStateException(e); }
        return rules;
    }

    @Override
    public void saveRule(RentTaskRule rule) {
        String sql = """
                INSERT INTO rent_task_rule(id, rent_id, frequency, auto_renew, day_of_week, day_of_month, month_of_year, title_prefix, description_prefix, last_generated_at, active)
                VALUES(?,?,?,?,?,?,?,?,?,?,?)
                ON CONFLICT(id) DO UPDATE SET
                frequency=excluded.frequency,
                auto_renew=excluded.auto_renew,
                day_of_week=excluded.day_of_week,
                day_of_month=excluded.day_of_month,
                month_of_year=excluded.month_of_year,
                title_prefix=excluded.title_prefix,
                description_prefix=excluded.description_prefix,
                last_generated_at=excluded.last_generated_at,
                active=excluded.active
                """;
        try (Connection c = database.openConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, rule.id().toString());
            ps.setString(2, rule.rentId().toString());
            ps.setString(3, rule.frequency());
            ps.setInt(4, rule.autoRenew() ? 1 : 0);
            if (rule.dayOfWeek() == null) ps.setNull(5, java.sql.Types.INTEGER); else ps.setInt(5, rule.dayOfWeek());
            if (rule.dayOfMonth() == null) ps.setNull(6, java.sql.Types.INTEGER); else ps.setInt(6, rule.dayOfMonth());
            if (rule.monthOfYear() == null) ps.setNull(7, java.sql.Types.INTEGER); else ps.setInt(7, rule.monthOfYear());
            ps.setString(8, rule.titlePrefix());
            ps.setString(9, rule.descriptionPrefix());
            if (rule.lastGeneratedAt() == null) ps.setNull(10, java.sql.Types.VARCHAR); else ps.setString(10, rule.lastGeneratedAt().toString());
            ps.setInt(11, rule.active() ? 1 : 0);
            ps.executeUpdate();
        } catch (SQLException e) { throw new IllegalStateException(e); }
    }

    @Override
    public void deleteRule(UUID ruleId) {
        try (Connection c = database.openConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM rent_task_rule WHERE id=?")) {
            ps.setString(1, ruleId.toString());
            ps.executeUpdate();
        } catch (SQLException e) { throw new IllegalStateException(e); }
    }

    @Override
    public void generateDueTasks() {
        String q = "SELECT r.id rent_id, r.contact_id, r.property_id, rr.* FROM rent_task_rule rr JOIN rent r ON r.id=rr.rent_id WHERE rr.active=1 AND rr.auto_renew=1";
        try (Connection c = database.openConnection(); PreparedStatement ps = c.prepareStatement(q); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                LocalDate today = LocalDate.now();
                String freq = rs.getString("frequency");
                Integer dow = (Integer) rs.getObject("day_of_week");
                Integer dom = (Integer) rs.getObject("day_of_month");
                Integer moy = (Integer) rs.getObject("month_of_year");
                String last = rs.getString("last_generated_at");
                LocalDate lastGen = last == null ? null : LocalDate.parse(last);
                if (!isDue(today, lastGen, freq, dow, dom, moy)) continue;

                String title = (rs.getString("title_prefix") == null ? "Tâche loyer" : rs.getString("title_prefix"))
                        + " - " + rs.getString("rent_id");
                String description = (rs.getString("description_prefix") == null ? "Générée automatiquement" : rs.getString("description_prefix"));

                try (PreparedStatement insert = c.prepareStatement("INSERT INTO task(id,title,description,status,due_date,created_at,completed_at,rent_id) VALUES(?,?,?,?,?,?,?,?)")) {
                    insert.setString(1, UUID.randomUUID().toString());
                    insert.setString(2, title);
                    insert.setString(3, description);
                    insert.setString(4, "TODO");
                    insert.setString(5, today.toString());
                    insert.setString(6, java.time.Instant.now().toString());
                    insert.setNull(7, java.sql.Types.VARCHAR);
                    insert.setString(8, rs.getString("rent_id"));
                    insert.executeUpdate();
                }

                try (PreparedStatement upd = c.prepareStatement("UPDATE rent_task_rule SET last_generated_at=? WHERE id=?")) {
                    upd.setString(1, today.toString());
                    upd.setString(2, rs.getString("id"));
                    upd.executeUpdate();
                }
            }
        } catch (SQLException e) { throw new IllegalStateException(e); }
    }

    private boolean isDue(LocalDate today, LocalDate lastGen, String freq, Integer dow, Integer dom, Integer moy) {
        if (lastGen != null && lastGen.isEqual(today)) return false;
        return switch (freq) {
            case "WEEKLY" -> dow != null && today.getDayOfWeek().getValue() == dow;
            case "MONTHLY" -> dom != null && today.getDayOfMonth() == dom;
            case "QUARTERLY" -> dom != null && moy != null && today.getDayOfMonth() == dom && today.getMonthValue() == moy;
            case "YEARLY" -> dom != null && moy != null && today.getDayOfMonth() == dom && today.getMonthValue() == moy;
            default -> false;
        };
    }
}
