package theider.log4jxmlview.logrecord;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;
import javax.xml.stream.XMLStreamException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Log4jXmlStreamReaderTest {

    private static final Logger logger = LoggerFactory.getLogger(Log4jXmlStreamReaderTest.class);
    
    @Test
    public void testProcessLog4jXml() throws IOException, XMLStreamException {
        // Arrange
        final AtomicInteger recordCount = new AtomicInteger(0);
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("logdatawithexception.xml");
        assert inputStream != null;
        // Execute
        Log4jXmlStreamReaderRecordHandler testRecordHandler = (LogRecord logRecord) -> {
            logger.debug("process record {} {}", recordCount, logRecord);
            recordCount.incrementAndGet();
        };        
        try {
            Log4jXmlStreamReader reader = new Log4jXmlStreamReader(inputStream, testRecordHandler);        
            reader.readLogRecords();
            // Assert
            assertEquals(12, recordCount.get());

        } catch(XMLStreamException ex) {
            logger.error("failed to parse", ex);
            fail("failed to parse XML");
        }
    }
}
