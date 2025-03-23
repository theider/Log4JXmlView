package theider.log4jxmlview.logrecord;

import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

public class LogRecordFileReader {

    public List<LogRecord> readLogFile(InputStream inputStream) throws LogRecordReaderException {
        try {
            List<LogRecord> records = new ArrayList<>();
            
            // Read the whole file as text
            String rawXml = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8).trim();
            
            // Wrap in a root element if not already
            if (!rawXml.startsWith("<records")) {
                rawXml = "<records>\n" + rawXml + "\n</records>";
            }
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(rawXml.getBytes(StandardCharsets.UTF_8)));
            
            NodeList nodeList = doc.getElementsByTagName("record");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element recordElement = (Element) nodeList.item(i);
                records.add(LogRecordFactory.fromElement(recordElement));
            }
            
            return records;
        } catch (IOException | ParserConfigurationException | SAXException ex) {
            throw new LogRecordReaderException("Error reading log records", ex);
        }
    }

}
