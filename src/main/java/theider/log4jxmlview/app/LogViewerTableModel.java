package theider.log4jxmlview.app;

import theider.log4jxmlview.logrecord.indexer.LogRecordIndexException;
import theider.log4jxmlview.logrecord.indexer.LogRecordIndex;
import theider.log4jxmlview.logrecord.model.LogRecord;

import javax.swing.table.AbstractTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theider.log4jxmlview.logrecord.xmlparser.LogRecordXmlParserException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class LogViewerTableModel extends AbstractTableModel {

    private static final Logger logger = LoggerFactory.getLogger(LogViewerTableModel.class);

    private static final DateTimeFormatter HUMAN_TS_FORMATTER
            = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
                    .withZone(ZoneId.systemDefault());
    
    private final String[] columnNames = {
        "Record", "Time", "Level", "Logger", "Message", "Thread", "Host", "Process", "Exception"
    };

    private final LogRecordIndex index;

    public LogViewerTableModel(LogRecordIndex index) {
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
                case 0 ->
                    rowIndex + 1;
                case 1 ->
                    (logRecord.timestamp() != null)
                    ? HUMAN_TS_FORMATTER.format(logRecord.timestamp())
                    : "";
                case 2 ->
                    logRecord.level();
                case 3 ->
                    logRecord.loggerName();
                case 4 ->
                    logRecord.message();
                case 5 ->
                    logRecord.threadName();
                case 6 ->
                    logRecord.hostName();
                case 7 ->
                    logRecord.processName();
                case 8 ->
                    (logRecord.exception() != null) ? logRecord.exception().exceptionType() : "";
                default ->
                    "";
            };
        } catch (LogRecordIndexException ex) {
            logger.error("Error reading record at index " + rowIndex, ex);
            return "[Error]";
        } catch (LogRecordXmlParserException ex) {
            logger.error("XML parse error " + rowIndex, ex);
            return "[Error]";
        }
    }
}
