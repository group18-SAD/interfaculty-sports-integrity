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
import java.sql.SQLException;

public class FlagsPanel extends JPanel implements Refreshable {

    // "Flagged" is the starting status set by the engine, so it is not offered here
    private static final String[] REVIEW_STATUSES = {"Under Investigation", "Cleared", "Penalized"};

    private final JTable flagTable = new JTable();
    private final JComboBox<String> statusBox = new JComboBox<>(REVIEW_STATUSES);
    private final JTextField noteField = new JTextField(30);
    private final JButton updateButton = new JButton("Update selected flag");

    public FlagsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(UiUtil.titled("Raised flags (click a row, then choose a status)", flagTable),
                BorderLayout.CENTER);

        boolean admin = CurrentUser.isAdmin();
        statusBox.setEnabled(admin);
        noteField.setEnabled(admin);
        updateButton.setEnabled(admin);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.add(new JLabel("New status:"));
        bottom.add(statusBox);
        bottom.add(new JLabel("Note:"));
        bottom.add(noteField);
        bottom.add(updateButton);
        JButton refreshButton = new JButton("Refresh");
        bottom.add(refreshButton);
        if (!admin) {
            bottom.add(new JLabel("Only administrators can change flag status."));
        }
        add(bottom, BorderLayout.SOUTH);

        updateButton.addActionListener(e -> updateSelected());
        refreshButton.addActionListener(e -> refresh());
    }

    @Override
    public void refresh() {
        try {
            flagTable.setModel(UiUtil.model(
                    Db.query("SELECT id, fixture_id, rule_code, reason, status, raised_at "
                           + "FROM flag ORDER BY raised_at DESC, id DESC"),
                    "ID", "Fixture", "Rule", "Reason", "Status", "Raised"));
        } catch (SQLException ex) {
            UiUtil.error(this, "Database error: " + ex.getMessage());
        }
    }

    private void updateSelected() {
        if (!CurrentUser.isAdmin()) {
            UiUtil.error(this, "Only administrators can change flag status.");
            return;
        }
        int row = flagTable.getSelectedRow();
        if (row < 0) {
            UiUtil.error(this, "Select a flag in the table first.");
            return;
        }
        try {
            int flagId = ((Number) flagTable.getValueAt(row, 0)).intValue();
            new FlagDAO().updateStatus(
                    flagId,
                    (String) statusBox.getSelectedItem(),
                    CurrentUser.username(),
                    UiUtil.blankToNull(noteField.getText()));
            noteField.setText("");
            refresh();
        } catch (SQLException ex) {
            UiUtil.error(this, "Database error: " + ex.getMessage());
        }
    }
}