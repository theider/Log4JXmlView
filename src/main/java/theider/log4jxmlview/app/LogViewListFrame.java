package theider.log4jxmlview.app;

import javax.swing.JInternalFrame;
import theider.log4jxmlview.logrecord.LogRecordReader;

public class LogViewListFrame extends JInternalFrame {
    
    private final LogRecordReader logRecordReader;

    public LogViewListFrame(LogRecordReader logFileRecordIndex) {
        super(logFileRecordIndex.getFilename(), true, true, true);
        this.logRecordReader = logFileRecordIndex;
    }
        
}
