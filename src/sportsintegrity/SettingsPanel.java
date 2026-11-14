package sportsintegrity;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.List;

public class SettingsPanel extends JPanel implements Refreshable {
    private final JComboBox<Item> sportBox = new JComboBox<>();
    private final JTextField thresholdField = new JTextField(6);
    private final JTextField newUserField = new JTextField(14);
    private final JPasswordField newPassField = new JPasswordField(14);
    private final JComboBox<String> roleBox = new JComboBox<>(new String[]{"CLERK", "ADMIN"});
    private final JLabel statusLabel = new JLabel(" ");

    public SettingsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel thresholdRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        thresholdRow.setBorder(BorderFactory.createTitledBorder("Rule thresholds (per sport)"));
        thresholdRow.add(new JLabel("Sport:"));
        thresholdRow.add(sportBox);
        thresholdRow.add(new JLabel("Margin threshold:"));
        thresholdRow.add(thresholdField);
        JButton saveThreshold = new JButton("Save threshold");
        thresholdRow.add(saveThreshold);

        JPanel userRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        userRow.setBorder(BorderFactory.createTitledBorder("User accounts"));
        userRow.add(new JLabel("Username:"));
        userRow.add(newUserField);
        userRow.add(new JLabel("Password (8+ characters):"));
        userRow.add(newPassField);
        userRow.add(new JLabel("Role:"));
        userRow.add(roleBox);
        JButton createUser = new JButton("Create account");
        userRow.add(createUser);

        JPanel sections = new JPanel(new GridLayout(2, 1, 0, 16));
        sections.add(thresholdRow);
        sections.add(userRow);

        add(sections, BorderLayout.NORTH);
        add(statusLabel, BorderLayout.SOUTH);

        sportBox.addActionListener(e -> loadThreshold());
        saveThreshold.addActionListener(e -> saveThreshold());
        createUser.addActionListener(e -> createUser());
    }

    @Override
    public void refresh() {
        try {
            UiUtil.fill(sportBox, Lookups.sports());
            loadThreshold();
        } catch (SQLException ex) {
            UiUtil.error(this, "Database error: " + ex.getMessage());
        }
    }

    private void loadThreshold() {
        Item sport = (Item) sportBox.getSelectedItem();
        if (sport == null) return;
        try {
            List<Object[]> rows = Db.query("SELECT margin_threshold FROM sport WHERE id = ?", sport.id);
            if (!rows.isEmpty()) {
                thresholdField.setText(String.valueOf(rows.get(0)[0]));
            }
        } catch (SQLException ex) {
            UiUtil.error(this, "Database error: " + ex.getMessage());
        }
    }

    private void saveThreshold() {
        Item sport = (Item) sportBox.getSelectedItem();
        if (sport == null) return;
        int value;
        try {
            value = Integer.parseInt(thresholdField.getText().trim());
        } catch (NumberFormatException ex) {
            UiUtil.error(this, "The threshold must be a whole number.");
            return;
        }
        if (value < 0) {
            UiUtil.error(this, "The threshold cannot be negative.");
            return;
        }
        try {
            Db.update("UPDATE sport SET margin_threshold = ? WHERE id = ?", value, sport.id);
            statusLabel.setText("Saved " + sport.label + " threshold as " + value
                    + ". It applies the next time checks run.");
        } catch (SQLException ex) {
            UiUtil.error(this, "Database error: " + ex.getMessage());
        }
    }

    private void createUser() {
        String username = UiUtil.blankToNull(newUserField.getText());
        String password = new String(newPassField.getPassword());
        String role = (String) roleBox.getSelectedItem();
        if (username == null || password.length() < 8) {
            UiUtil.error(this, "Enter a username and a password of at least 8 characters.");
            return;
        }
        try {
            Db.update("INSERT INTO app_user (username, password_hash, role) VALUES (?, ?, ?)",
                    username, PasswordUtil.hash(password), role);
            newUserField.setText("");
            newPassField.setText("");
            statusLabel.setText("Created " + role + " account for " + username + ".");
        } catch (SQLException ex) {
            UiUtil.error(this, ex.getErrorCode() == 1062
                    ? "That username already exists."
                    : "Database error: " + ex.getMessage());
        } catch (Exception ex) {
            UiUtil.error(this, "Could not create the account: " + ex.getMessage());
        }
    }
}