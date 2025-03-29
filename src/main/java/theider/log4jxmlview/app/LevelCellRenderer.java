package theider.log4jxmlview.app;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class LevelCellRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table,
            Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {

        Component c = super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);

        String level = (value != null) ? value.toString() : "";

        // Set default foreground/background
        c.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
        c.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());

        if (!isSelected) {
            switch (level.toUpperCase()) {
                case "ERROR" -> c.setBackground(new Color(255, 102, 102)); // light red
                case "WARN"  -> c.setBackground(new Color(255, 204, 102)); // light orange
                case "INFO"  -> c.setBackground(new Color(204, 255, 204)); // light green
                case "DEBUG" -> c.setBackground(new Color(204, 229, 255)); // light blue
                case "TRACE" -> c.setBackground(new Color(224, 224, 224)); // light gray
            }
        }

        return c;
    }
}
