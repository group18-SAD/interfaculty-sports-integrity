package sportsintegrity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class WalkoverRule implements AnomalyRule {
    private static final int MIN_FORFEITS = 2;

    @Override
    public String code() {
        return "R4";
    }

    @Override
    public Optional<String> evaluate(MatchData m, Connection conn) throws SQLException {
        if (m.forfeitFacultyId == null) return Optional.empty();

        String sql = "SELECT COUNT(*) FROM result r JOIN fixture f ON f.id = r.fixture_id "
                + "WHERE f.sport_id = ? AND r.forfeit_faculty_id = ?";
        int forfeits = 0;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, m.sportId);
            ps.setInt(2, m.forfeitFacultyId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) forfeits = rs.getInt(1);
            }
        }

        if (forfeits >= MIN_FORFEITS) {
            return Optional.of("Faculty has forfeited " + forfeits + " " + m.sportName
                    + " matches this season");
        }
        return Optional.empty();
    }
}