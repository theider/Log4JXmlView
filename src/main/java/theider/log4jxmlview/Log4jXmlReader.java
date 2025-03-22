package theider.log4jxmlview;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.xml.parsers.*;

public class Log4jXmlReader {

    private static final Logger log = LoggerFactory.getLogger(Log4jXmlReader.class);

    public List<LogRecord> parseRecords(InputStream inputStream) throws Exception {
        List<LogRecord> records = new ArrayList<>();

        try {
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
                records.add(LogRecord.fromElement(recordElement));
            }
            
        } catch (Exception e) {
            log.error("Failed to parse XML file", e);
            throw e;
        }

        return records;
    }

    private String getText(Element parent, String tag) {
        NodeList list = parent.getElementsByTagName(tag);
        return list.getLength() > 0 ? list.item(0).getTextContent() : "";
    }
}
