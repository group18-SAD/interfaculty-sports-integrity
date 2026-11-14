package sportsintegrity;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class Lookups {
    private Lookups() {}

    public static List<Item> faculties() throws SQLException {
        return toItems(Db.query("SELECT id, code FROM faculty ORDER BY code"));
    }

    public static List<Item> sports() throws SQLException {
        return toItems(Db.query("SELECT id, name FROM sport ORDER BY name"));
    }

    public static List<Item> fixtures() throws SQLException {
        return toItems(Db.query(
                "SELECT f.id, CONCAT(f.fixture_date, '  ', s.name, ': ', h.code, ' vs ', a.code) "
              + "FROM fixture f "
              + "JOIN sport s ON s.id = f.sport_id "
              + "JOIN faculty h ON h.id = f.home_faculty_id "
              + "JOIN faculty a ON a.id = f.away_faculty_id "
              + "ORDER BY f.fixture_date DESC, f.id DESC"));
    }

    private static List<Item> toItems(List<Object[]> rows) {
        List<Item> items = new ArrayList<>();
        for (Object[] row : rows) {
            items.add(new Item(((Number) row[0]).intValue(), String.valueOf(row[1])));
        }
        return items;
    }
}