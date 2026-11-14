package sportsintegrity;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EditResultPanel extends JPanel implements Refreshable {
    private final JTable table = new JTable();
    private final JLabel statusLabel = new JLabel(" ");
    private final JButton editButton = new JButton("Edit selected result");
    private final JButton lockButton = new JButton("Lock / unlock selected");

    public EditResultPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(UiUtil.titled("Saved results (select a row)", table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.add(editButton);
        lockButton.setVisible(CurrentUser.isAdmin());
        bottom.add(lockButton);
        bottom.add(statusLabel);
        add(bottom, BorderLayout.SOUTH);

        editButton.addActionListener(e -> openEditor());
        lockButton.addActionListener(e -> toggleLock());
    }

    @Override
    public void refresh() {
        try {
            table.setModel(UiUtil.model(Db.query(
                    "SELECT r.fixture_id, f.fixture_date, s.name, "
                  + "CONCAT(h.code, ' ', r.home_score, ' - ', r.away_score, ' ', a.code), "
                  + "COALESCE(r.referee, ''), CASE WHEN r.locked = 1 THEN 'Locked' ELSE 'Open' END "
                  + "FROM result r "
                  + "JOIN fixture f ON f.id = r.fixture_id "
                  + "JOIN sport s ON s.id = f.sport_id "
                  + "JOIN faculty h ON h.id = f.home_faculty_id "
                  + "JOIN faculty a ON a.id = f.away_faculty_id "
                  + "ORDER BY f.fixture_date DESC, r.fixture_id DESC"),
                    "Fixture", "Date", "Sport", "Score", "Referee", "State"));
        } catch (SQLException ex) {
            UiUtil.error(this, "Database error: " + ex.getMessage());
        }
    }

    private Integer selectedFixtureId() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UiUtil.error(this, "Select a result in the table first.");
            return null;
        }
        return ((Number) table.getValueAt(row, 0)).intValue();
    }

    private void openEditor() {
        Integer fixtureId = selectedFixtureId();
        if (fixtureId == null) return;

        try {
            List<Object[]> rows = Db.query(
                    "SELECT r.home_score, r.away_score, COALESCE(r.referee, ''), r.forfeit_faculty_id, "
                  + "r.locked, f.home_faculty_id, f.away_faculty_id "
                  + "FROM result r JOIN fixture f ON f.id = r.fixture_id WHERE r.fixture_id = ?",
                    fixtureId);
            Object[] current = rows.get(0);
            boolean locked = ((Number) current[4]).intValue() == 1;
            if (locked && !CurrentUser.isAdmin()) {
                UiUtil.error(this, "This result is locked. Ask an administrator to unlock it.");
                return;
            }
            int homeId = ((Number) current[5]).intValue();
            int awayId = ((Number) current[6]).intValue();

            JTextField homeField = new JTextField(String.valueOf(current[0]), 4);
            JTextField awayField = new JTextField(String.valueOf(current[1]), 4);
            JTextField refereeField = new JTextField(String.valueOf(current[2]), 20);
            JTextField reasonField = new JTextField(25);

            List<Item> options = new ArrayList<>();
            options.add(new Item(0, "None"));
            for (Item faculty : Lookups.faculties()) {
                if (faculty.id == homeId || faculty.id == awayId) options.add(faculty);
            }
            JComboBox<Item> forfeitBox = new JComboBox<>();
            UiUtil.fill(forfeitBox, options);
            int currentForfeit = current[3] == null ? 0 : ((Number) current[3]).intValue();
            for (int i = 0; i < forfeitBox.getItemCount(); i++) {
                if (forfeitBox.getItemAt(i).id == currentForfeit) {
                    forfeitBox.setSelectedIndex(i);
                }
            }

            JPanel form = new JPanel(new GridLayout(0, 2, 8, 6));
            form.add(new JLabel("Home score:"));                form.add(homeField);
            form.add(new JLabel("Away score:"));                form.add(awayField);
            form.add(new JLabel("Referee / umpire:"));          form.add(refereeField);
            form.add(new JLabel("Forfeit by:"));                form.add(forfeitBox);
            form.add(new JLabel("Reason for change (required):")); form.add(reasonField);

            int choice = JOptionPane.showConfirmDialog(this, form,
                    "Edit result for fixture " + fixtureId,
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (choice != JOptionPane.OK_OPTION) return;

            int newHome;
            int newAway;
            try {
                newHome = Integer.parseInt(homeField.getText().trim());
                newAway = Integer.parseInt(awayField.getText().trim());
            } catch (NumberFormatException ex) {
                UiUtil.error(this, "Scores must be whole numbers.");
                return;
            }
            if (newHome < 0 || newAway < 0) {
                UiUtil.error(this, "Scores cannot be negative.");
                return;
            }
            String reason = UiUtil.blankToNull(reasonField.getText());
            if (reason == null) {
                UiUtil.error(this, "Enter a reason for the change.");
                return;
            }

            Item forfeit = (Item) forfeitBox.getSelectedItem();
            Integer newForfeit = (forfeit == null || forfeit.id == 0) ? null : forfeit.id;
            String newReferee = UiUtil.blankToNull(refereeField.getText());

            Db.update(
                    "UPDATE result SET home_score = ?, away_score = ?, referee = ?, "
                  + "forfeit_faculty_id = ?, last_edited_by = ?, last_edited_at = NOW() "
                  + "WHERE fixture_id = ?",
                    newHome, newAway, newReferee, newForfeit, CurrentUser.username(), fixtureId);

            Db.update(
                    "INSERT INTO result_audit (fixture_id, changed_by, old_values, new_values, reason) "
                  + "VALUES (?, ?, ?, ?, ?)",
                    fixtureId, CurrentUser.username(),
                    describe(current[0], current[1], current[2], current[3]),
                    describe(newHome, newAway, newReferee == null ? "" : newReferee, newForfeit),
                    reason);

            int flags = new AnomalyEngine().runForFixture(fixtureId);
            statusLabel.setText("Saved changes to fixture " + fixtureId
                    + ". Flags now raised by checks: " + flags);
            refresh();
        } catch (SQLException ex) {
            UiUtil.error(this, "Database error: " + ex.getMessage());
        }
    }

    private void toggleLock() {
        Integer fixtureId = selectedFixtureId();
        if (fixtureId == null) return;
        if (!CurrentUser.isAdmin()) {
            UiUtil.error(this, "Only administrators can lock or unlock results.");
            return;
        }
        try {
            List<Object[]> rows = Db.query("SELECT locked FROM result WHERE fixture_id = ?", fixtureId);
            boolean wasLocked = ((Number) rows.get(0)[0]).intValue() == 1;

            Db.update("UPDATE result SET locked = ? WHERE fixture_id = ?",
                    wasLocked ? 0 : 1, fixtureId);
            Db.update(
                    "INSERT INTO result_audit (fixture_id, changed_by, old_values, new_values, reason) "
                  + "VALUES (?, ?, ?, ?, ?)",
                    fixtureId, CurrentUser.username(),
                    wasLocked ? "Locked" : "Open",
                    wasLocked ? "Open" : "Locked",
                    "Lock status changed");

            statusLabel.setText("Fixture " + fixtureId + " is now " + (wasLocked ? "open" : "locked") + ".");
            refresh();
        } catch (SQLException ex) {
            UiUtil.error(this, "Database error: " + ex.getMessage());
        }
    }

    private static String describe(Object home, Object away, Object referee, Object forfeit) {
        return home + "-" + away
                + " | referee: " + (referee == null ? "" : referee)
                + " | forfeit: " + (forfeit == null ? "none" : forfeit);
    }
}