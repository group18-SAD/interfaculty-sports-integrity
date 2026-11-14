package sportsintegrity;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::start);
    }

    /** Shows the sign-in window, then the main window. Called again after logout. */
    static void start() {
        try {
            LoginDialog login = new LoginDialog();
            login.setVisible(true);
            if (!login.isLoggedIn()) {
                System.exit(0);
            }
            new MainFrame(Main::start).setVisible(true);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null,
                    "Cannot connect to the database:\n" + ex.getMessage()
                    + "\n\nStart MySQL in XAMPP and check db.properties.",
                    "Database error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
}