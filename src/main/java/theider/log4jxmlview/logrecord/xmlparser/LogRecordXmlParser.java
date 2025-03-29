package theider.log4jxmlview.logrecord.xmlparser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import theider.log4jxmlview.logrecord.model.LogException;
import theider.log4jxmlview.logrecord.model.LogRecord;
import theider.log4jxmlview.logrecord.model.StackFrame;

public class LogRecordXmlParser {

    private String frameClass;
    private String frameMethod;
    private int frameLine;
    
    private void resetFrameFields() {
        frameClass = null;
        frameMethod = null;
        frameLine = -1;
    }
    
    public LogRecord fromInputStream(InputStream xmlStream) throws LogRecordXmlParserException {

        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader reader = factory.createXMLStreamReader(xmlStream, "UTF-8");

            ParserState state = ParserState.ParserScanningToRecord;
            Map<String, String> fieldMap = new HashMap<>();
            List<StackFrame> stackFrames = new ArrayList<>();
            LogException logException = null;

            String exceptionType = null, exceptionMessage = null;
            resetFrameFields();

            while (reader.hasNext()) {
                int event = reader.next();

                switch (state) {
                    case ParserScanningToRecord -> {
                        if (event == XMLStreamConstants.START_ELEMENT
                                && reader.getLocalName().equals("record")) {
                            fieldMap.clear();
                            stackFrames.clear();
                            logException = null;
                            state = ParserState.ParserLoadingFields;
                        }
                    }

                    case ParserLoadingFields -> {
                        if (event == XMLStreamConstants.START_ELEMENT) {
                            String tag = reader.getLocalName();
                            if (tag.equals("exception")) {
                                state = ParserState.ParserLoadingException;
                            } else {
                                String text = reader.getElementText();
                                fieldMap.put(tag, text);
                            }
                        } else if (event == XMLStreamConstants.END_ELEMENT
                                && reader.getLocalName().equals("record")) {
                            return new LogRecord(
                                    fieldMap.get("timestamp"),
                                    parseLong(fieldMap.get("sequence")),
                                    fieldMap.get("loggerClassName"),
                                    fieldMap.get("loggerName"),
                                    fieldMap.get("level"),
                                    fieldMap.get("message"),
                                    fieldMap.get("threadName"),
                                    parseLong(fieldMap.get("threadId")),
                                    fieldMap.get("hostName"),
                                    fieldMap.get("processName"),
                                    parseLong(fieldMap.get("processId")),
                                    logException
                            );
                        }
                    }

                    case ParserLoadingException -> {
                        if (event == XMLStreamConstants.START_ELEMENT) {
                            String tag = reader.getLocalName();
                            switch (tag) {
                                case "exceptionType" -> {
                                    exceptionType = reader.getElementText();
                                }
                                case "message" -> {
                                    exceptionMessage = reader.getElementText();
                                }
                                case "frames" -> {
                                    state = ParserState.ParserLoadingFrames;
                                }
                                case "causedBy" -> {
                                    LogException inner = parseNestedException(reader);
                                    logException = new LogException(exceptionType, exceptionMessage, new ArrayList<>(stackFrames), inner);
                                    state = ParserState.ParserLoadingFields;
                                }
                            }
                        } else if (event == XMLStreamConstants.END_ELEMENT
                                && reader.getLocalName().equals("exception")) {
                            if (logException == null) {
                                logException = new LogException(exceptionType, exceptionMessage, new ArrayList<>(stackFrames), null);
                            }
                            state = ParserState.ParserLoadingFields;
                        }
                    }

                    case ParserLoadingFrames -> {
                        if (event == XMLStreamConstants.START_ELEMENT
                                && reader.getLocalName().equals("frame")) {
                            resetFrameFields();
                            state = ParserState.ParserLoadingFrame;
                        } else if (event == XMLStreamConstants.END_ELEMENT
                                && reader.getLocalName().equals("frames")) {
                            state = ParserState.ParserLoadingException;
                        }
                    }

                    case ParserLoadingFrame -> {
                        if (event == XMLStreamConstants.START_ELEMENT) {
                            switch (reader.getLocalName()) {
                                case "class" ->
                                    frameClass = reader.getElementText();
                                case "method" ->
                                    frameMethod = reader.getElementText();
                                case "line" -> {
                                    try {
                                        frameLine = Integer.parseInt(reader.getElementText());
                                    } catch (NumberFormatException e) {                                    
                                        frameLine = -1;
                                    }
                                }
                            }
                        } else if (event == XMLStreamConstants.END_ELEMENT
                                && reader.getLocalName().equals("frame")) {
                            stackFrames.add(new StackFrame(frameClass, frameMethod, frameLine));
                            resetFrameFields();
                            state = ParserState.ParserLoadingFrames;
                        }
                    }
                }
            }
        } catch(XMLStreamException ex) {
            throw new LogRecordXmlParserException("XML parse failure", ex);
        }
        throw new LogRecordXmlParserException("XML log record end never found.");
    }

    private static Long parseLong(String str) {
        return (str != null && !str.isEmpty()) ? Long.valueOf(str) : null;
    }

    private enum ParserState {
        ParserScanningToRecord,
        ParserLoadingFields,
        ParserLoadingException,
        ParserLoadingFrames,
        ParserLoadingFrame
    }

    private LogException parseNestedException(XMLStreamReader reader) throws XMLStreamException {
        String nestedType = null, nestedMessage = null;
        List<StackFrame> nestedFrames = new ArrayList<>();

        while (reader.hasNext()) {
            int event = reader.next();

            if (event == XMLStreamConstants.START_ELEMENT) {
                String tag = reader.getLocalName();
                switch (tag) {
                    case "exceptionType" ->
                        nestedType = reader.getElementText();
                    case "message" ->
                        nestedMessage = reader.getElementText();
                    case "frame" ->
                        nestedFrames.add(parseFrame(reader));
                    case "causedBy" -> {
                        LogException deeper = parseNestedException(reader);
                        return new LogException(nestedType, nestedMessage, nestedFrames, deeper);
                    }
                }
            } else if (event == XMLStreamConstants.END_ELEMENT
                    && reader.getLocalName().equals("exception")) {
                break;
            }
        }
        return new LogException(nestedType, nestedMessage, nestedFrames, null);
    }

    private StackFrame parseFrame(XMLStreamReader reader) throws XMLStreamException {
        String className = null, methodName = null;
        int line = -1;
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                switch (reader.getLocalName()) {
                    case "class" ->
                        className = reader.getElementText();
                    case "method" ->
                        methodName = reader.getElementText();
                    case "line" ->
                        line = Integer.parseInt(reader.getElementText());
                }
            } else if (event == XMLStreamConstants.END_ELEMENT
                    && reader.getLocalName().equals("frame")) {
                break;
            }
        }
        return new StackFrame(className, methodName, line);
    }

}
