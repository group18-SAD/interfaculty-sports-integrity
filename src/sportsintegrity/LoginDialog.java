package sportsintegrity;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.List;

public class LoginDialog extends JDialog {
    private final boolean firstRun;
    private boolean loggedIn = false;

    private final JTextField userField = new JTextField(16);
    private final JPasswordField passField = new JPasswordField(16);
    private final JPasswordField confirmField = new JPasswordField(16);

    public LoginDialog() throws SQLException {
        super((Frame) null, "Sign in", true);
        firstRun = Db.scalar("SELECT COUNT(*) FROM app_user") == 0;

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Username:"));
        form.add(userField);
        form.add(new JLabel("Password:"));
        form.add(passField);
        if (firstRun) {
            form.add(new JLabel("Confirm password:"));
            form.add(confirmField);
        }

        JLabel heading = new JLabel(firstRun
                ? "No accounts yet. Create the first administrator account."
                : "Sign in to continue.");

        JButton button = new JButton(firstRun ? "Create administrator" : "Sign in");
        button.addActionListener(e -> submit());
        getRootPane().setDefaultButton(button);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        root.add(heading, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        root.add(button, BorderLayout.SOUTH);

        setContentPane(root);
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    private void submit() {
        String username = UiUtil.blankToNull(userField.getText());
        String password = new String(passField.getPassword());
        try {
            if (username == null || password.isEmpty()) {
                throw new IllegalArgumentException("Enter a username and password.");
            }
            if (firstRun) {
                createFirstAdmin(username, password);
            } else {
                signIn(username, password);
            }
            loggedIn = true;
            dispose();
        } catch (Exception ex) {
            UiUtil.error(this, ex.getMessage());
        }
    }

    private void createFirstAdmin(String username, String password) throws Exception {
        String confirm = new String(confirmField.getPassword());
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters.");
        }
        if (!password.equals(confirm)) {
            throw new IllegalArgumentException("The passwords do not match.");
        }
        Db.update("INSERT INTO app_user (username, password_hash, role) VALUES (?, ?, 'ADMIN')",
                username, PasswordUtil.hash(password));
        CurrentUser.set(username, "ADMIN");
    }

    private void signIn(String username, String password) throws Exception {
        List<Object[]> rows = Db.query(
                "SELECT password_hash, role FROM app_user WHERE username = ?", username);
        if (rows.isEmpty() || !PasswordUtil.verify(password, (String) rows.get(0)[0])) {
            throw new IllegalArgumentException("Wrong username or password.");
        }
        CurrentUser.set(username, (String) rows.get(0)[1]);
    }
}