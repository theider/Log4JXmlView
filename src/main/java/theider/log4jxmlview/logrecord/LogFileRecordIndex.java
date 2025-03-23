package theider.log4jxmlview.logrecord;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class LogFileRecordIndex {
    
    private static final Logger logger = LoggerFactory.getLogger(LogFileRecordIndex.class);
    
    private final List<LogFileRecordIndexEntry> entries = new ArrayList<>();

    private final File sourceLogFile;

    public LogFileRecordIndex(File sourceLogFile) throws IOException {
        this.sourceLogFile = sourceLogFile;
        this.loadFileIndex();
    }

    public String getFilename() {
        return sourceLogFile.getName();
    }
    
    private void loadFileIndex() throws IOException {       
        logger.debug(getFilename() + ": Building index...");
        try (RandomAccessFile raf = new RandomAccessFile(sourceLogFile, "r")) {
            long offset = 0;
            int index = 0;
            String line;
            while ((line = raf.readLine()) != null) {
                if (line.trim().startsWith("<record")) {
                    entries.add(new LogFileRecordIndexEntry(index++, offset, line.length()));
                }
                offset = raf.getFilePointer(); // track offset for next line
                logger.debug("Indexing record at offset " + offset);
            }
            logger.debug(getFilename() + ": Index built with " + entries.size() + " entries");
        }        
    }

    public int size() {
        return entries.size();
    }

    public Optional<LogRecord> readRecordAt(int recordIndex) throws LogRecordIndexException {
        if (recordIndex < 0 || recordIndex >= entries.size()) {
            return Optional.empty();
        }
    
        LogFileRecordIndexEntry entry = entries.get(recordIndex);
        String rawXml = null;
        try (RandomAccessFile raf = new RandomAccessFile(sourceLogFile, "r")) {
            raf.seek(entry.getOffset());
    
            // Read only the exact number of bytes for the record
            byte[] bytes = new byte[entry.getSizeBytes()];
            int bytesRead = raf.read(bytes);
            if (bytesRead != entry.getSizeBytes()) {
                throw new IOException("Incomplete read: expected " + entry.getSizeBytes() + " bytes, got " + bytesRead);
            }
    
            rawXml = new String(bytes, StandardCharsets.UTF_8).trim();
    
            // Parse without wrapping
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(rawXml.getBytes(StandardCharsets.UTF_8)));
            Element recordElement = (Element) doc.getElementsByTagName("record").item(0);
    
            return Optional.of(LogRecordFactory.fromElement(recordElement));
    
        } catch (IOException | ParserConfigurationException | SAXException ex) {
            throw new LogRecordIndexException("Error reading log record at index " + recordIndex + "[" + rawXml + "]", ex);
        }
    }
    
}
