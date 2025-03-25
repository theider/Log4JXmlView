package theider.log4jxmlview.logrecord;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Log4jXmlStreamReader implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(Log4jXmlStreamReader.class);

    private final InputStream inputStream;
    
    private final Log4jXmlStreamReaderRecordHandler recordHandler;

    public Log4jXmlStreamReader(InputStream inputStream, Log4jXmlStreamReaderRecordHandler recordHandler) {
        this.inputStream = inputStream;
        this.recordHandler = recordHandler;
    }

    private void handleLoadingFields(XMLStreamReader reader, int event) throws XMLStreamException {
        switch(event) {
            case XMLStreamConstants.START_ELEMENT -> {
                switch(reader.getLocalName()) {
                    case "exception" -> {
                        exceptionStackFrames.clear();
                        exceptionType = null;
                        exceptionMessage = null;
                        parserState = ParserState.ParserLoadingException;                                
                    }
                    default -> logRecordDataFieldsMaps.put(reader.getLocalName(), reader.getElementText());
                }                        
            }                
            case XMLStreamConstants.END_ELEMENT -> {
                switch(reader.getLocalName()) {                            
                    case "record" -> {
                        // Create a new record and call the handler.
                        LogRecord logRecord = new LogRecord(
                            logRecordDataFieldsMaps.get("timestamp"),
                            logRecordDataFieldsMaps.containsKey("sequence") ? Long.valueOf(logRecordDataFieldsMaps.get("sequence")) : null,
                            logRecordDataFieldsMaps.get("loggerClassName"),
                            logRecordDataFieldsMaps.get("loggerName"),
                            logRecordDataFieldsMaps.get("level"),
                            logRecordDataFieldsMaps.get("message"),
                            logRecordDataFieldsMaps.get("threadName"),
                            logRecordDataFieldsMaps.containsKey("threadId") ? Long.valueOf(logRecordDataFieldsMaps.get("threadId")) : null,
                            logRecordDataFieldsMaps.get("hostName"),
                            logRecordDataFieldsMaps.get("processName"),
                            logRecordDataFieldsMaps.containsKey("processId") ? Long.valueOf(logRecordDataFieldsMaps.get("processId")) : null,
                            logException
                        );
                        if(recordHandler != null) {
                            recordHandler.handleRecord(logRecord);
                        }                                
                        parserState = ParserState.ParserScanningToRecord;
                    }
                }
            }
        }
    }

    private void handlerScanningToRecord(XMLStreamReader reader, int event) {
        switch(event) {
            case XMLStreamConstants.START_ELEMENT -> {
                switch(reader.getLocalName()) {
                    case "record" -> {
                        logRecordDataFieldsMaps.clear();
                        logException = null;
                        parserState = ParserState.ParserLoadingFields;                
                    }
                }
            }                
        }
    }

    private void handleLoadingException(XMLStreamReader reader, int event) throws XMLStreamException {
        switch(event) {
            case XMLStreamConstants.START_ELEMENT -> {
                switch(reader.getLocalName()) {
                    case "exceptionType" -> exceptionType = reader.getElementText();
                    case "message" -> exceptionMessage = reader.getElementText();
                    case "frames" -> parserState = ParserState.ParserLoadingFrames;
                }
            }
            case XMLStreamConstants.END_ELEMENT -> {
                switch(reader.getLocalName()) {
                    // Switch back to loading fields.
                    case "exception" -> {                        
                        this.logException = new LogException(exceptionType, exceptionMessage, new ArrayList<>(exceptionStackFrames));
                        parserState = ParserState.ParserLoadingFields;
                    }
                }
            }
        }
    }

    private void handleLoadingFrames(XMLStreamReader reader, int event) throws XMLStreamException {
        switch(event) {
            case XMLStreamConstants.START_ELEMENT -> {
                switch(reader.getLocalName()) {
                    case "frame" -> parserState = ParserState.ParserLoadingFrame;
                    case "message" -> exceptionMessage = reader.getElementText();
                    case "frames" -> parserState = ParserState.ParserLoadingFrames;
                }
            }
            case XMLStreamConstants.END_ELEMENT -> {
                switch(reader.getLocalName()) {
                    // Switch back to loading fields.
                    case "frames" -> parserState = ParserState.ParserLoadingException;
                }
            }
        }
    }
    
    private void handleLoadingFrame(XMLStreamReader reader, int event) throws XMLStreamException {
        switch(event) {
            case XMLStreamConstants.START_ELEMENT -> {
                switch(reader.getLocalName()) {
                    case "class" -> exceptionFrameClassName = reader.getElementText();
                    case "method" -> exceptionFrameMethodName = reader.getElementText();
                    case "line" -> exceptionFrameLineNumber = Integer.parseInt(reader.getElementText());                    
                }
            }
            case XMLStreamConstants.END_ELEMENT -> {
                switch(reader.getLocalName()) {
                    // Switch back to loading frames to get next frame or resume data fields.
                    case "frame" -> {
                        exceptionStackFrames.add(new StackFrame(exceptionFrameClassName, exceptionFrameMethodName, exceptionFrameLineNumber));
                        parserState = ParserState.ParserLoadingFrames;
                    }
                }
            }
        }
    }

    private enum ParserState {
        ParserScanningToRecord,
        ParserLoadingFields,
        ParserLoadingException,
        ParserLoadingFrames,
        ParserLoadingFrame
    }
                  
    private ParserState parserState;
    
    private final Map<String, String> logRecordDataFieldsMaps = new HashMap<>();
    
    private LogException logException = null;
    
    private String exceptionType;
    
    private String exceptionMessage;
    
    private String exceptionFrameClassName;
    
    private String exceptionFrameMethodName;
    
    private int exceptionFrameLineNumber;
    
    private final List<StackFrame> exceptionStackFrames = new ArrayList<>();
    
    private void processState(int event, XMLStreamReader reader) throws XMLStreamException {        
        try {
            switch(parserState) {
                case ParserScanningToRecord -> handlerScanningToRecord(reader, event);
                case ParserLoadingFields -> handleLoadingFields(reader, event);
                case ParserLoadingException -> handleLoadingException(reader, event);            
                case ParserLoadingFrames -> handleLoadingFrames(reader, event);
                case ParserLoadingFrame -> handleLoadingFrame(reader, event);
            }
        } catch(XMLStreamException ex) {
            logger.error("XML exception reading log at {} state {}", reader.getLocation(), parserState);
            throw ex;
        }
    }

    public void readLogRecords() throws IOException, XMLStreamException {
        this.parserState = ParserState.ParserScanningToRecord;
        try (BufferedInputStream in = new BufferedInputStream(new LogDataWrapperStream(inputStream))) {        
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader reader = factory.createXMLStreamReader(in);

            while (reader.hasNext()) {
                int event = reader.next();
                processState(event, reader);                
            }
        }
    }

    @Override
    public void close() throws IOException {
        if(inputStream != null) {
            inputStream.close();
        }
    }

}
