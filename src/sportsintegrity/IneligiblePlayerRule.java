package sportsintegrity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class IneligiblePlayerRule implements AnomalyRule {
    @Override
    public String code() {
        return "R3";
    }

    @Override
    public Optional<String> evaluate(MatchData m, Connection conn) throws SQLException {
        String sql = "SELECT l.reg_no FROM lineup l "
                + "LEFT JOIN roster ro ON ro.faculty_id = l.faculty_id AND ro.reg_no = l.reg_no "
                + "WHERE l.fixture_id = ? AND ro.reg_no IS NULL";

        List<String> missing = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, m.fixtureId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    missing.add(rs.getString(1));
                }
            }
        }

        if (!missing.isEmpty()) {
            return Optional.of("Not on submitted roster: " + String.join(", ", missing));
        }
        return Optional.empty();
    }
}