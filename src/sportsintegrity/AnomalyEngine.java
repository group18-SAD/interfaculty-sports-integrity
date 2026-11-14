package sportsintegrity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class AnomalyEngine {
    private final List<AnomalyRule> rules = List.of(
            new LargeMarginRule(),
            new RepeatedPairingRule(),
            new IneligiblePlayerRule(),
            new WalkoverRule(),
            new RefereeBiasRule());

    private final FlagDAO flagDAO = new FlagDAO();

    /** Runs every rule on one fixture that has a saved result. Returns the number of flags raised. */
    public int runForFixture(int fixtureId) throws SQLException {
        int raised = 0;
        try (Connection c = DBConnection.getConnection()) {
            MatchData m = loadMatch(fixtureId, c);
            if (m == null) {
                System.out.println("No result found for fixture " + fixtureId);
                return 0;
            }
            flagDAO.deleteUnreviewedFlags(fixtureId);
            for (AnomalyRule rule : rules) {
                Optional<String> reason = rule.evaluate(m, c);
                if (reason.isPresent()) {
                    flagDAO.insertFlag(fixtureId, rule.code(), reason.get());
                    System.out.println(rule.code() + ": " + reason.get());
                    raised++;
                }
            }
        }
        return raised;
    }

    private MatchData loadMatch(int fixtureId, Connection c) throws SQLException {
        String sql = "SELECT f.id, f.sport_id, s.name, s.margin_threshold, "
                + "f.home_faculty_id, f.away_faculty_id, r.home_score, r.away_score, "
                + "r.referee, r.forfeit_faculty_id "
                + "FROM fixture f JOIN sport s ON s.id = f.sport_id "
                + "JOIN result r ON r.fixture_id = f.id WHERE f.id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, fixtureId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new MatchData(
                        rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getInt(4),
                        rs.getInt(5), rs.getInt(6), rs.getInt(7), rs.getInt(8),
                        rs.getString(9), (Integer) rs.getObject(10));
            }
        }
    }
}