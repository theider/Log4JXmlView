package theider.log4jxmlview;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

public class LogViewerTableModel extends AbstractTableModel {

    private final String[] columnNames = {
        "Timestamp", "Level", "Logger", "Message", "Thread", "Host", "Process"
    };

    private List<LogRecord> records = new ArrayList<>();

    public void setData(List<LogRecord> records) {
        this.records = records;
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return records.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        LogRecord r = records.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> r.timestamp();
            case 1 -> r.level();
            case 2 -> r.loggerName();
            case 3 -> r.message();
            case 4 -> r.threadName();
            case 5 -> r.hostName();
            case 6 -> r.processName();
            default -> "";
        };
    }
}
