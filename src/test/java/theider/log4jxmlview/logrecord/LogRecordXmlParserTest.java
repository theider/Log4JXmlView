package theider.log4jxmlview.logrecord;

import theider.log4jxmlview.logrecord.model.StackFrame;
import theider.log4jxmlview.logrecord.model.LogException;
import theider.log4jxmlview.logrecord.model.LogRecord;
import theider.log4jxmlview.logrecord.xmlparser.LogRecordXmlParser;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

public class LogRecordXmlParserTest {

    @Test
    public void testParseSingleRecordWithException() throws Exception {
        URL resource = getClass().getClassLoader().getResource("logrecord1158.xml");
        assertNotNull(resource, "Test file logrecord1158.xml not found in resources");

        File file = new File(resource.toURI());
        byte[] xmlBytes = Files.readAllBytes(file.toPath());

        LogRecordXmlParser parser = new LogRecordXmlParser();

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(xmlBytes)) {
            LogRecord record = parser.fromInputStream(inputStream);

            assertNotNull(record);
            assertEquals("2025-03-21T16:03:12.774Z", record.timestamp());
            assertEquals("ERROR", record.level());
            assertEquals("Exception while dispatching incoming RPC call", record.message());
            assertEquals("default task-1", record.threadName());
            assertEquals("app1", record.hostName());
            assertEquals("jboss-modules.jar", record.processName());
            assertEquals(30266L, record.processId());

            LogException ex = record.exception();
            assertNotNull(ex);
            assertEquals("com.google.gwt.user.client.rpc.SerializationException", ex.exceptionType());
            assertTrue(ex.message().contains("GetContactHandler$1"), "Exception message should reference missing type");

            assertNotNull(ex.frames());
            assertFalse(ex.frames().isEmpty(), "Exception should have stack frames");

            StackFrame first = ex.frames().get(0);
            assertEquals("com.google.gwt.user.server.rpc.impl.ServerSerializationStreamWriter", first.className());
            assertEquals("serialize", first.methodName());
            assertEquals(699, first.lineNumber());

            for (StackFrame frame : ex.frames()) {
                assertNotNull(frame.className());
                assertNotNull(frame.methodName());
                assertTrue(frame.lineNumber() >= -1);
            }
        }
    }

}
