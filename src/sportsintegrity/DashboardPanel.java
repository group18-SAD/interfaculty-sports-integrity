package sportsintegrity;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DashboardPanel extends JPanel implements Refreshable {
    private static final String[] STATUSES = {"Flagged", "Under Investigation", "Cleared", "Penalized"};

    private final JTable summaryTable = new JTable();
    private final JTable ruleTable = new JTable();
    private final JTable sportTable = new JTable();

    public DashboardPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refresh());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(refreshButton);
        add(top, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(1, 3, 12, 0));
        grid.add(UiUtil.titled("Summary", summaryTable));
        grid.add(UiUtil.titled("Flags by rule", ruleTable));
        grid.add(UiUtil.titled("Flags by sport", sportTable));
        add(grid, BorderLayout.CENTER);
    }

    @Override
    public void refresh() {
        try {
            List<Object[]> summary = new ArrayList<>();
            summary.add(new Object[]{"Fixtures", Db.scalar("SELECT COUNT(*) FROM fixture")});
            summary.add(new Object[]{"Results entered", Db.scalar("SELECT COUNT(*) FROM result")});
            summary.add(new Object[]{"Flags raised", Db.scalar("SELECT COUNT(*) FROM flag")});
            for (String status : STATUSES) {
                summary.add(new Object[]{
                        "Flags: " + status,
                        Db.scalar("SELECT COUNT(*) FROM flag WHERE status = ?", status)});
            }
            summaryTable.setModel(UiUtil.model(summary, "Measure", "Value"));

            ruleTable.setModel(UiUtil.model(
                    Db.query("SELECT rule_code, COUNT(*) FROM flag GROUP BY rule_code ORDER BY rule_code"),
                    "Rule", "Flags"));

            sportTable.setModel(UiUtil.model(
                    Db.query("SELECT s.name, COUNT(fl.id) FROM sport s "
                           + "LEFT JOIN fixture f ON f.sport_id = s.id "
                           + "LEFT JOIN flag fl ON fl.fixture_id = f.id "
                           + "GROUP BY s.name ORDER BY s.name"),
                    "Sport", "Flags"));
        } catch (SQLException ex) {
            UiUtil.error(this, "Database error: " + ex.getMessage());
        }
    }
}