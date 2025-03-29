package theider.log4jxmlview.logrecord;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import javax.xml.stream.XMLStreamException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogRecordReader {

    private static final Logger logger = LoggerFactory.getLogger(LogRecordReader.class);
    
    private final File sourceLogFile;

    private final LogRecordOffsetIndex offsetIndex;

    private final LogRecordXmlParser logRecordXmlParser;

    public LogRecordReader(File sourceLogFile, ILogRecordIndexProgressListener progressListener, long fileSizeBytes) throws IOException {
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

    public LogRecord readRecordAt(int recordIndex) throws LogRecordIndexException {
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
        } catch (IOException | XMLStreamException ex) {
            String xmlText = xmlBytes == null ? null : new String(xmlBytes);
            //throw new LogRecordIndexException("Error reading log record at index " + recordIndex, ex);
            throw new LogRecordIndexException("Error reading log record at index " + recordIndex + " data:[" + xmlText + "]", ex);
        } 
    }
}
