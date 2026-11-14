package sportsintegrity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class RepeatedPairingRule implements AnomalyRule {
    private static final int MIN_MEETINGS = 3;

    @Override
    public String code() {
        return "R1";
    }

    @Override
    public Optional<String> evaluate(MatchData m, Connection conn) throws SQLException {
        Integer winner = m.winnerId();
        if (winner == null) return Optional.empty();

        String sql = "SELECT f.home_faculty_id, f.away_faculty_id, r.home_score, r.away_score "
                + "FROM fixture f JOIN result r ON r.fixture_id = f.id "
                + "WHERE f.sport_id = ? AND ((f.home_faculty_id = ? AND f.away_faculty_id = ?) "
                + "OR (f.home_faculty_id = ? AND f.away_faculty_id = ?))";

        int meetings = 0;
        int winnerWins = 0;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, m.sportId);
            ps.setInt(2, m.homeFacultyId);
            ps.setInt(3, m.awayFacultyId);
            ps.setInt(4, m.awayFacultyId);
            ps.setInt(5, m.homeFacultyId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    meetings++;
                    Integer w = MatchData.winnerOf(rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getInt(4));
                    if (winner.equals(w)) winnerWins++;
                }
            }
        }

        if (meetings >= MIN_MEETINGS && winnerWins == meetings) {
            return Optional.of("The same two faculties met " + meetings + " times in "
                    + m.sportName + " and one faculty won every meeting");
        }
        return Optional.empty();
    }
}