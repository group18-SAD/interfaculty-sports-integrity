package sportsintegrity;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class ResultsPanel extends JPanel implements Refreshable {
    private final JComboBox<Item> sportBox = new JComboBox<>();
    private final JComboBox<Item> homeBox = new JComboBox<>();
    private final JComboBox<Item> awayBox = new JComboBox<>();
    private final JComboBox<Item> forfeitBox = new JComboBox<>();
    private final JTextField dateField = new JTextField(LocalDate.now().toString(), 12);
    private final JTextField homeScoreField = new JTextField(4);
    private final JTextField awayScoreField = new JTextField(4);
    private final JTextField refereeField = new JTextField(20);
    private final JTextArea homeLineup = new JTextArea(8, 18);
    private final JTextArea awayLineup = new JTextArea(8, 18);
    private final JLabel statusLabel = new JLabel(" ");

    public ResultsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel form = new JPanel(new GridLayout(0, 4, 8, 6));
        form.add(new JLabel("Sport:"));                  form.add(sportBox);
        form.add(new JLabel("Date (yyyy-mm-dd):"));      form.add(dateField);
        form.add(new JLabel("Home faculty:"));           form.add(homeBox);
        form.add(new JLabel("Away faculty:"));           form.add(awayBox);
        form.add(new JLabel("Home score:"));             form.add(homeScoreField);
        form.add(new JLabel("Away score:"));             form.add(awayScoreField);
        form.add(new JLabel("Referee / umpire:"));       form.add(refereeField);
        form.add(new JLabel("Forfeit by (optional):"));  form.add(forfeitBox);

        JPanel lineups = new JPanel(new GridLayout(1, 2, 12, 0));
        lineups.add(UiUtil.titled("Home lineup (registration numbers, one per line)", homeLineup));
        lineups.add(UiUtil.titled("Away lineup (registration numbers, one per line)", awayLineup));

        JButton saveButton = new JButton("Save result and run checks");
        saveButton.addActionListener(e -> save());
        JPanel bottom = new JPanel(new BorderLayout(10, 0));
        bottom.add(saveButton, BorderLayout.WEST);
        bottom.add(statusLabel, BorderLayout.CENTER);

        add(form, BorderLayout.NORTH);
        add(lineups, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    @Override
    public void refresh() {
        try {
            UiUtil.fill(sportBox, Lookups.sports());
            List<Item> faculties = Lookups.faculties();
            UiUtil.fill(homeBox, faculties);
            UiUtil.fill(awayBox, faculties);

            List<Item> forfeitOptions = new ArrayList<>();
            forfeitOptions.add(new Item(0, "None"));
            forfeitOptions.addAll(faculties);
            UiUtil.fill(forfeitBox, forfeitOptions);
        } catch (SQLException ex) {
            UiUtil.error(this, "Database error: " + ex.getMessage());
        }
    }

    private void save() {
        Item sport = (Item) sportBox.getSelectedItem();
        Item home = (Item) homeBox.getSelectedItem();
        Item away = (Item) awayBox.getSelectedItem();
        Item forfeit = (Item) forfeitBox.getSelectedItem();

        if (sport == null || home == null || away == null) {
            UiUtil.error(this, "Choose a sport and both faculties.");
            return;
        }
        if (home.id == away.id) {
            UiUtil.error(this, "Home and away faculty must be different.");
            return;
        }
        Integer forfeitId = (forfeit == null || forfeit.id == 0) ? null : forfeit.id;
        if (forfeitId != null && forfeitId != home.id && forfeitId != away.id) {
            UiUtil.error(this, "A forfeiting faculty must be the home or away faculty.");
            return;
        }

        LocalDate date;
        int homeGoals;
        int awayGoals;
        try {
            date = LocalDate.parse(dateField.getText().trim());
            homeGoals = Integer.parseInt(homeScoreField.getText().trim());
            awayGoals = Integer.parseInt(awayScoreField.getText().trim());
        } catch (DateTimeParseException | NumberFormatException ex) {
            UiUtil.error(this, "Use yyyy-mm-dd for the date and whole numbers for the scores.");
            return;
        }
        if (homeGoals < 0 || awayGoals < 0) {
            UiUtil.error(this, "Scores cannot be negative.");
            return;
        }

        try {
            int fixtureId = Db.insert(
                    "INSERT INTO fixture (sport_id, home_faculty_id, away_faculty_id, fixture_date) "
                  + "VALUES (?, ?, ?, ?)",
                    sport.id, home.id, away.id, java.sql.Date.valueOf(date));

            Db.update(
                    "INSERT INTO result (fixture_id, home_score, away_score, referee, "
                  + "forfeit_faculty_id, entered_by) VALUES (?, ?, ?, ?, ?, ?)",
                    fixtureId, homeGoals, awayGoals,
                    UiUtil.blankToNull(refereeField.getText()),
                    forfeitId,
                    CurrentUser.username());

            saveLineup(fixtureId, home.id, homeLineup.getText());
            saveLineup(fixtureId, away.id, awayLineup.getText());

            int flags = new AnomalyEngine().runForFixture(fixtureId);
            statusLabel.setText("Saved fixture " + fixtureId + ". Flags raised: " + flags);

            homeScoreField.setText("");
            awayScoreField.setText("");
        } catch (SQLException ex) {
            UiUtil.error(this, "Database error: " + ex.getMessage());
        }
    }

    private void saveLineup(int fixtureId, int facultyId, String text) throws SQLException {
        for (String line : text.split("\\R")) {
            String regNo = line.trim();
            if (!regNo.isEmpty()) {
                Db.update("INSERT IGNORE INTO lineup (fixture_id, faculty_id, reg_no) VALUES (?, ?, ?)",
                        fixtureId, facultyId, regNo);
            }
        }
    }
}