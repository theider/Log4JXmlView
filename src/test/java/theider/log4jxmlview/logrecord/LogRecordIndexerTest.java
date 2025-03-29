package theider.log4jxmlview.logrecord;

import theider.log4jxmlview.logrecord.indexer.LogRecordOffsetIndex;
import theider.log4jxmlview.logrecord.indexer.LogRecordIndexer;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogRecordIndexerTest {

    private static final Logger logger = LoggerFactory.getLogger(LogRecordIndexerTest.class);

    @Test
    public void testIndexLogRecords() throws Exception {
        URL resource = getClass().getClassLoader().getResource("logdatawithexception.xml");
        assertNotNull(resource, "Test file logdatawithexception.xml not found in resources");
        File file = new File(resource.toURI());
        long fileSizeBytes = file.length();
        
        
        // First pass: build index
        try (InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream("logdatawithexception.xml")) {

            LogRecordIndexer indexer = new LogRecordIndexer();
            LogRecordOffsetIndex recordIndex = indexer.indexRecords(inputStream, 
                    (read, total) -> {
                        double percent = (read * 100.0) / total;
                        logger.trace("Progress: {}%", String.format("%.2f", percent));
                    }, fileSizeBytes);    
            
            assertFalse(recordIndex.getRecordCount() == 0, "No <record> entries found in the XML");

            // Second pass: verify offset and parse each record
            try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                for (int i = 0; i < recordIndex.getRecordCount(); i++) {
                    long recordOffset = recordIndex.getRecordOffset(i);
                    long recordLength = recordIndex.getRecordSize(i);
                    logger.trace("Record {}: offset={}, length={}", i, recordOffset, recordLength);

                    assertTrue(recordOffset >= 0, "Start offset should be non-negative");
                    assertTrue(recordLength > 0, "Record length should be positive");

                    // Read record XML bytes
                    byte[] buffer = new byte[(int) recordLength];
                    raf.seek(recordOffset);
                    raf.readFully(buffer);
                    // Parse using XMLStreamReader
                    XMLInputFactory factory = XMLInputFactory.newInstance();
                    try (InputStream recordStream = new java.io.ByteArrayInputStream(buffer)) {
                        XMLStreamReader reader = factory.createXMLStreamReader(recordStream, StandardCharsets.UTF_8.name());

                        boolean recordFound = false;
                        while (reader.hasNext()) {
                            int event = reader.next();
                            if (event == XMLStreamConstants.START_ELEMENT &&
                                "record".equals(reader.getLocalName())) {
                                recordFound = true;
                            } else if (event == XMLStreamConstants.END_ELEMENT &&
                                       "record".equals(reader.getLocalName())) {
                                break;
                            }
                        }

                        assertTrue(recordFound, "Valid <record> tag should be found and parsed");
                    }
                }
            }
        }
    }
}
