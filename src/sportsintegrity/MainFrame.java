package sportsintegrity;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Component;

public class MainFrame extends JFrame {

    public MainFrame(Runnable onLogout) {
        super("Interfaculty Sports Integrity Detector");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        ResultsPanel results = new ResultsPanel();
        EditResultPanel editResults = new EditResultPanel();
        EligibilityPanel eligibility = new EligibilityPanel();
        FlagsPanel flags = new FlagsPanel();
        DashboardPanel dashboard = new DashboardPanel();

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Results", results);
        tabs.addTab("Edit Results", editResults);
        tabs.addTab("Eligibility", eligibility);
        tabs.addTab("Flags & Status", flags);
        tabs.addTab("Dashboard", dashboard);
        if (CurrentUser.isAdmin()) {
            tabs.addTab("Settings", new SettingsPanel());
        }

        tabs.addChangeListener(e -> {
            Component selected = tabs.getSelectedComponent();
            if (selected instanceof Refreshable) {
                ((Refreshable) selected).refresh();
            }
        });

        JLabel who = new JLabel("Signed in as " + CurrentUser.username()
                + " (" + CurrentUser.role() + ")");
        JButton logout = new JButton("Log out");
        logout.addActionListener(e -> {
            CurrentUser.clear();
            dispose();
            onLogout.run();
        });

        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        header.add(who, BorderLayout.WEST);
        header.add(logout, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        setSize(1100, 720);
        setLocationRelativeTo(null);
        results.refresh();
    }
}