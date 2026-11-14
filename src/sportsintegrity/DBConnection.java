package sportsintegrity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DBConnection {
    private static final String DEFAULT_URL =
        "jdbc:mysql://localhost:3306/sports_integrity"
        + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    private static Properties settings;

    private DBConnection() {}

    private static synchronized Properties settings() {
        if (settings == null) {
            settings = new Properties();
            File file = new File(System.getProperty("user.dir"), "db.properties");
            try (FileInputStream in = new FileInputStream(file)) {
                settings.load(in);
            } catch (IOException ignored) {
                // No file found: fall back to XAMPP defaults below
            }
        }
        return settings;
    }

    public static Connection getConnection() throws SQLException {
        Properties p = settings();
        return DriverManager.getConnection(
                p.getProperty("db.url", DEFAULT_URL),
                p.getProperty("db.user", "root"),
                p.getProperty("db.password", ""));
    }
}