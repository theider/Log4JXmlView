package theider.log4jxmlview.logrecord;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public interface Log4jXmlStreamReaderRecordHandler {

    public void handleRecord(XMLStreamReader reader) throws XMLStreamException;
    
}
