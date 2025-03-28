package theider.log4jxmlview.logrecord;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import javax.xml.stream.XMLStreamException;

public class LogRecordReader {

    private final File sourceLogFile;

    private final LogRecordOffsetIndex offsetIndex;

    private final LogRecordFactory factory;

    public LogRecordReader(File sourceLogFile) throws IOException {
        this.sourceLogFile = sourceLogFile;
        try (InputStream in = new FileInputStream(sourceLogFile)) {
            LogRecordIndexer indexer = new LogRecordIndexer();
            this.offsetIndex = indexer.indexRecords(in);
        }
        this.factory = new LogRecordFactory();
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

        try (RandomAccessFile raf = new RandomAccessFile(sourceLogFile, "r")) {
            byte[] xmlBytes = new byte[(int) length];            
            raf.seek(offset);
            raf.readFully(xmlBytes);
            InputStream in = new ByteArrayInputStream(xmlBytes);
            LogRecord record = factory.fromInputStream(in);            
            return record;
        } catch (IOException | XMLStreamException ex) {
            throw new LogRecordIndexException("Error reading log record at index " + recordIndex, ex);
        } 
    }
}
