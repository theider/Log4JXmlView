package theider.log4jxmlview.logrecord.indexer;

import theider.log4jxmlview.logrecord.model.LogRecord;
import theider.log4jxmlview.logrecord.xmlparser.LogRecordXmlParser;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theider.log4jxmlview.logrecord.xmlparser.LogRecordXmlParserException;

public class LogRecordIndex {

    private static final Logger logger = LoggerFactory.getLogger(LogRecordIndex.class);
    
    private final File sourceLogFile;

    private final LogRecordOffsetIndex offsetIndex;

    private final LogRecordXmlParser logRecordXmlParser;

    public LogRecordIndex(File sourceLogFile, ILogRecordIndexProgressListener progressListener, long fileSizeBytes) throws IOException {
        this.sourceLogFile = sourceLogFile;
        try (InputStream in = new FileInputStream(sourceLogFile)) {
            logger.debug("create log record reader {} {}", sourceLogFile.getAbsolutePath(), fileSizeBytes);
            LogRecordIndexer indexer = new LogRecordIndexer();
            this.offsetIndex = indexer.indexRecords(in, progressListener, fileSizeBytes);
        }
        this.logRecordXmlParser = new LogRecordXmlParser();
    }

    public String getFilename() {
        return sourceLogFile.getName();
    }

    public int size() {
        return offsetIndex.getRecordCount();
    }

    public LogRecord readRecordAt(int recordIndex) throws LogRecordIndexException, LogRecordXmlParserException {
        if (recordIndex < 0 || recordIndex >= offsetIndex.getRecordCount()) {
            throw new LogRecordIndexException("Log record index out of bounds:" + recordIndex);
        }

        long offset = offsetIndex.getRecordOffset(recordIndex);
        long length = offsetIndex.getRecordSize(recordIndex);
        if(length <= 0) {
            throw new LogRecordIndexException("Invalid length found at record index " + recordIndex + " len=" + length);
        }

        byte[] xmlBytes = null;
        try (RandomAccessFile raf = new RandomAccessFile(sourceLogFile, "r")) {
            xmlBytes = new byte[(int) length];            
            raf.seek(offset);
            raf.readFully(xmlBytes);
            InputStream in = new ByteArrayInputStream(xmlBytes);
            LogRecord record = logRecordXmlParser.fromInputStream(in);            
            return record;
        } catch (IOException ex) {
            String xmlText = xmlBytes == null ? null : new String(xmlBytes);
            //throw new LogRecordIndexException("Error reading log record at index " + recordIndex, ex);
            throw new LogRecordIndexException("Error reading log record at index " + recordIndex + " data:[" + xmlText + "]", ex);
        } 
    }
}
