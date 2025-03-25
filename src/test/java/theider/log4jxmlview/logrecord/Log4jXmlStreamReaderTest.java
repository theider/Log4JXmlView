package theider.log4jxmlview.logrecord;

import org.junit.jupiter.api.Test;
import java.io.InputStream;

public class Log4jXmlStreamReaderTest {

    @Test
    public void testProcessLog4jXml() throws Exception {
        // Arrange
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("logdatawithexception.xml");
        assert inputStream != null;
        
        Log4jXmlStreamReader reader = new Log4jXmlStreamReader(inputStream);
        reader.readLogRecords();

    }
}
