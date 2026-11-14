package sportsintegrity;

import java.sql.Connection;
import java.util.Optional;

public class LargeMarginRule implements AnomalyRule {
    @Override
    public String code() {
        return "R2";
    }

    @Override
    public Optional<String> evaluate(MatchData m, Connection conn) {
        if (m.margin() > m.marginThreshold) {
            return Optional.of("Margin " + m.margin() + " exceeds the " + m.sportName
                    + " threshold of " + m.marginThreshold);
        }
        return Optional.empty();
    }
}