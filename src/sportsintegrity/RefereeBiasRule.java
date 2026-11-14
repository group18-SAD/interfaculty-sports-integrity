package sportsintegrity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class RefereeBiasRule implements AnomalyRule {
    private static final int MIN_DECIDED = 3;

    @Override
    public String code() {
        return "R5";
    }

    @Override
    public Optional<String> evaluate(MatchData m, Connection conn) throws SQLException {
        Integer winner = m.winnerId();
        if (winner == null || m.referee == null) return Optional.empty();

        String sql = "SELECT f.home_faculty_id, f.away_faculty_id, r.home_score, r.away_score "
                + "FROM result r JOIN fixture f ON f.id = r.fixture_id "
                + "WHERE f.sport_id = ? AND r.referee = ?";

        int decided = 0;
        int sameWinner = 0;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, m.sportId);
            ps.setString(2, m.referee);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Integer w = MatchData.winnerOf(rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getInt(4));
                    if (w != null) {
                        decided++;
                        if (winner.equals(w)) sameWinner++;
                    }
                }
            }
        }

        if (decided >= MIN_DECIDED && sameWinner == decided) {
            return Optional.of("Referee " + m.referee + " has " + decided + " decided "
                    + m.sportName + " matches, all won by the same faculty");
        }
        return Optional.empty();
    }
}