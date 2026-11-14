package sportsintegrity;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.List;

public class EligibilityPanel extends JPanel implements Refreshable {
    private final JComboBox<Item> facultyBox = new JComboBox<>();
    private final JTextField regField = new JTextField(14);
    private final JTextField nameField = new JTextField(22);
    private final JTable rosterTable = new JTable();
    private final JComboBox<Item> fixtureBox = new JComboBox<>();
    private final JTable checkTable = new JTable();
    private final JLabel statusLabel = new JLabel(" ");

    public EligibilityPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Top section: add players to a faculty's submitted roster
        JPanel addRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addRow.add(new JLabel("Faculty:"));
        addRow.add(facultyBox);
        addRow.add(new JLabel("Reg no:"));
        addRow.add(regField);
        addRow.add(new JLabel("Full name:"));
        addRow.add(nameField);
        JButton addButton = new JButton("Add to roster");
        addRow.add(addButton);

        JPanel rosterSection = new JPanel(new BorderLayout(0, 8));
        rosterSection.add(addRow, BorderLayout.NORTH);
        rosterSection.add(UiUtil.titled("Submitted roster for the selected faculty", rosterTable),
                BorderLayout.CENTER);

        // Bottom section: check one fixture's lineups against the rosters
        JPanel checkRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        checkRow.add(new JLabel("Fixture:"));
        checkRow.add(fixtureBox);
        JButton checkButton = new JButton("Check lineups");
        checkRow.add(checkButton);

        JPanel checkSection = new JPanel(new BorderLayout(0, 8));
        checkSection.add(checkRow, BorderLayout.NORTH);
        checkSection.add(UiUtil.titled("Lineup check", checkTable), BorderLayout.CENTER);

        JPanel split = new JPanel(new GridLayout(2, 1, 0, 12));
        split.add(rosterSection);
        split.add(checkSection);

        add(split, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        facultyBox.addActionListener(e -> loadRoster());
        addButton.addActionListener(e -> addPlayer());
        checkButton.addActionListener(e -> checkLineup());
    }

    @Override
    public void refresh() {
        try {
            UiUtil.fill(facultyBox, Lookups.faculties());
            UiUtil.fill(fixtureBox, Lookups.fixtures());
            loadRoster();
        } catch (SQLException ex) {
            UiUtil.error(this, "Database error: " + ex.getMessage());
        }
    }

    private void loadRoster() {
        Item faculty = (Item) facultyBox.getSelectedItem();
        if (faculty == null) return;
        try {
            rosterTable.setModel(UiUtil.model(
                    Db.query("SELECT reg_no, full_name FROM roster WHERE faculty_id = ? ORDER BY reg_no",
                            faculty.id),
                    "Reg no", "Full name"));
        } catch (SQLException ex) {
            UiUtil.error(this, "Database error: " + ex.getMessage());
        }
    }

    private void addPlayer() {
        Item faculty = (Item) facultyBox.getSelectedItem();
        String regNo = UiUtil.blankToNull(regField.getText());
        String name = UiUtil.blankToNull(nameField.getText());
        if (faculty == null || regNo == null || name == null) {
            UiUtil.error(this, "Choose a faculty and enter a registration number and full name.");
            return;
        }
        try {
            Db.update(
                    "INSERT INTO roster (faculty_id, reg_no, full_name) VALUES (?, ?, ?) "
                  + "ON DUPLICATE KEY UPDATE full_name = VALUES(full_name)",
                    faculty.id, regNo, name);
            regField.setText("");
            nameField.setText("");
            statusLabel.setText("Added " + regNo + " to the " + faculty.label + " roster.");
            loadRoster();
        } catch (SQLException ex) {
            UiUtil.error(this, "Database error: " + ex.getMessage());
        }
    }

    private void checkLineup() {
        Item fixture = (Item) fixtureBox.getSelectedItem();
        if (fixture == null) {
            UiUtil.error(this, "Choose a fixture.");
            return;
        }
        try {
            List<Object[]> rows = Db.query(
                    "SELECT fa.code, l.reg_no, "
                  + "CASE WHEN ro.reg_no IS NULL THEN 'Not on roster' ELSE 'Verified' END AS status "
                  + "FROM lineup l "
                  + "JOIN faculty fa ON fa.id = l.faculty_id "
                  + "LEFT JOIN roster ro ON ro.faculty_id = l.faculty_id AND ro.reg_no = l.reg_no "
                  + "WHERE l.fixture_id = ? ORDER BY status, l.reg_no",
                    fixture.id);
            checkTable.setModel(UiUtil.model(rows, "Faculty", "Reg no", "Status"));

            long notOnRoster = rows.stream().filter(r -> "Not on roster".equals(r[2])).count();
            statusLabel.setText(rows.isEmpty()
                    ? "No lineup saved for this fixture."
                    : notOnRoster + " player(s) not on the submitted roster.");
        } catch (SQLException ex) {
            UiUtil.error(this, "Database error: " + ex.getMessage());
        }
    }
}