package sportsintegrity;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;
import java.awt.Component;
import java.util.List;

public final class UiUtil {

    static void error(SettingsPanel aThis, String string) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    private UiUtil() {}

    /** A table model that users cannot edit by typing into cells. */
    public static DefaultTableModel model(List<Object[]> rows, String... headers) {
        return new DefaultTableModel(rows.toArray(new Object[0][]), headers) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    public static void fill(JComboBox<Item> box, List<Item> items) {
        box.removeAllItems();
        for (Item item : items) {
            box.addItem(item);
        }
    }

    public static JScrollPane titled(String title, Component component) {
        JScrollPane pane = new JScrollPane(component);
        pane.setBorder(BorderFactory.createTitledBorder(title));
        return pane;
    }

    public static String blankToNull(String text) {
        if (text == null) return null;
        String trimmed = text.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static void error(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Problem", JOptionPane.ERROR_MESSAGE);
    }
}