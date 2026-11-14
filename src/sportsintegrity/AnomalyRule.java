package sportsintegrity;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public interface AnomalyRule {
    String code();
    Optional<String> evaluate(MatchData match, Connection conn) throws SQLException;
}