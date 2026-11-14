package sportsintegrity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class FlagDAO {

    public void insertFlag(int fixtureId, String ruleCode, String reason) throws SQLException {
        Db.update("INSERT INTO flag (fixture_id, rule_code, reason) VALUES (?, ?, ?)",
                fixtureId, ruleCode, reason);
    }

    public void deleteUnreviewedFlags(int fixtureId) throws SQLException {
        Db.update("DELETE FROM flag WHERE fixture_id = ? AND status = 'Flagged'", fixtureId);
    }

    /** Changes a flag's status and records the change in status_history, in one transaction. */
    public void updateStatus(int flagId, String newStatus, String changedBy, String note)
            throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement("UPDATE flag SET status = ? WHERE id = ?")) {
                ps.setString(1, newStatus);
                ps.setInt(2, flagId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO status_history (flag_id, status, changed_by, note) VALUES (?, ?, ?, ?)")) {
                ps.setInt(1, flagId);
                ps.setString(2, newStatus);
                ps.setString(3, changedBy);
                ps.setString(4, note);
                ps.executeUpdate();
            }
            c.commit();
        }
    }
}