package theider.log4jxmlview.app;

import theider.log4jxmlview.logrecord.*;

import javax.swing.table.AbstractTableModel;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogViewerTableModel extends AbstractTableModel {

    private static final Logger logger = LoggerFactory.getLogger(LogViewerTableModel.class);
    
    private final String[] columnNames = {
        "Timestamp", "Level", "Logger", "Message", "Thread", "Host", "Process", "Exception"
    };

    private final LogFileRecordIndex index;

    public LogViewerTableModel(LogFileRecordIndex index) {
        this.index = index;
    }

    @Override
    public int getRowCount() {
        return index.size();
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
        try {
            Optional<LogRecord> optional = index.readRecordAt(rowIndex);
            if (optional.isEmpty()) return "";

            LogRecord r = optional.get();
            return switch (columnIndex) {
                case 0 -> r.timestamp();
                case 1 -> r.level();
                case 2 -> r.loggerName();
                case 3 -> r.message();
                case 4 -> r.threadName();
                case 5 -> r.hostName();
                case 6 -> r.processName();
                case 7 -> (r.exception() != null) ? r.exception().exceptionType() : "";
                default -> "";
            };
        } catch (LogRecordIndexException e) {
            logger.error("Error reading record at index " + rowIndex, e);
            return "[Error]";
        }
    }
}
