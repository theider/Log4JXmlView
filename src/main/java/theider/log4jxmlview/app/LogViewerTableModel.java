package theider.log4jxmlview.app;

import theider.log4jxmlview.logrecord.*;

import javax.swing.table.AbstractTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogViewerTableModel extends AbstractTableModel {

    private static final Logger logger = LoggerFactory.getLogger(LogViewerTableModel.class);
    
    private final String[] columnNames = {
        "Record", "Timestamp", "Level", "Logger", "Message", "Thread", "Host", "Process", "Exception"
    };

    private final LogRecordReader index;

    public LogViewerTableModel(LogRecordReader index) {
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
            LogRecord logRecord = index.readRecordAt(rowIndex);
            
            return switch (columnIndex) {
                case 0 -> rowIndex + 1;
                case 1 -> logRecord.timestamp();
                case 2 -> logRecord.level();
                case 3 -> logRecord.loggerName();
                case 4 -> logRecord.message();
                case 5 -> logRecord.threadName();
                case 6 -> logRecord.hostName();
                case 7 -> logRecord.processName();
                case 8 -> (logRecord.exception() != null) ? logRecord.exception().exceptionType() : "";
                default -> "";
            };
        } catch (LogRecordIndexException e) {
            logger.error("Error reading record at index " + rowIndex, e);
            return "[Error]";
        }
    }
}
