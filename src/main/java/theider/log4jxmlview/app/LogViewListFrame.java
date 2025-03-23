package theider.log4jxmlview.app;

import javax.swing.JInternalFrame;
import theider.log4jxmlview.logrecord.LogFileRecordIndex;

public class LogViewListFrame extends JInternalFrame {
    
    private final LogFileRecordIndex logFileRecordIndex;

    public LogViewListFrame(LogFileRecordIndex logFileRecordIndex) {
        super(logFileRecordIndex.getFilename(), true, true, true);
        this.logFileRecordIndex = logFileRecordIndex;
    }
        
}
