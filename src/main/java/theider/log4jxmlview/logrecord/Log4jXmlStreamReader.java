package theider.log4jxmlview.logrecord;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Log4jXmlStreamReader implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(Log4jXmlStreamReader.class);

    private final InputStream inputStream;

    public Log4jXmlStreamReader(InputStream inputStream) {
        this.inputStream = inputStream;
    }
    
    protected enum ParserState {
        ParserScanningToRecord,
        ParserLoadingFields        
    }
                  
    private ParserState parserState;
    
    private String fieldTimestamp;
    
    private Long fieldSequence;
    
    private void processState(int event, XMLStreamReader reader) throws XMLStreamException {
        logger.debug("processState {} {} {}", parserState, event, reader.getLocalName());
        switch(parserState) {
            case ParserScanningToRecord -> {
                switch(event) {
                    case XMLStreamConstants.START_ELEMENT -> {
                        switch(reader.getLocalName()) {
                            case "record" -> {
                                logger.debug("record start");
                                fieldTimestamp = null;
                                fieldSequence = null;
                                parserState = ParserState.ParserLoadingFields;                
                            }
                        }
                    }                
                }
            }
            case ParserLoadingFields -> {
                switch(event) {
                    case XMLStreamConstants.START_ELEMENT -> {
                        switch(reader.getLocalName()) {
                            case "timestamp" -> {
                                fieldTimestamp = reader.getElementText();
                                logger.debug("timestamp {}", fieldTimestamp);                                                
                            }
                            case "sequence" -> {
                                fieldSequence = Long.valueOf(reader.getElementText());
                                logger.debug("sequence {}", fieldSequence);                                                
                            }
                        }
                    }                
                    case XMLStreamConstants.END_ELEMENT -> {
                        switch(reader.getLocalName()) {
                            case "record" -> {
                                logger.debug("create new record {}", fieldTimestamp);
                            }
                        }
                    }
                }
            }
        }
    }

    public void readLogRecords() throws IOException, XMLStreamException {
        this.parserState = ParserState.ParserScanningToRecord;
        try (BufferedInputStream in = new BufferedInputStream(inputStream)) {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader reader = factory.createXMLStreamReader(in);

            while (reader.hasNext()) {
                int event = reader.next();
                processState(event, reader);                
            }
        }
    }

    private void processRecord(XMLStreamReader reader) throws XMLStreamException {
        logger.debug("----- Record -----");

        // Read attributes or nested elements inside <record>
        while (reader.hasNext()) {
            int event = reader.next();
            switch(event) {
                case XMLStreamConstants.START_ELEMENT -> {
                    String tag = reader.getLocalName();

                    switch (tag) {
                        case "date", "level", "logger", "message" -> System.out.println(tag + ": " + reader.getElementText());
                        case "exception" -> {
                            logger.error("parse failure {}", reader.getElementText());
                        }
                        default -> {
                        }
                    }
                }
                case XMLStreamConstants.END_ELEMENT -> {
                }
            }
            //"record".equals(reader.getLocalName())) {
                    }
    }   

    @Override
    public void close() throws IOException {
        if(inputStream != null) {
            inputStream.close();
        }
    }

}
