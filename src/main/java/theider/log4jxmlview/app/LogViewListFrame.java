package theider.log4jxmlview.app;

import javax.swing.JInternalFrame;
import theider.log4jxmlview.logrecord.indexer.LogRecordIndex;

public class LogViewListFrame extends JInternalFrame {
    
    public LogViewListFrame(LogRecordIndex logFileRecordIndex) {
        super(logFileRecordIndex.getFilename(), true, true, true);
    }
        
}
