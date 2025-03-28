package theider.log4jxmlview.logrecord;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class LogRecordFactory {

    public LogRecord fromInputStream(InputStream xmlStream) throws XMLStreamException {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader reader = factory.createXMLStreamReader(xmlStream, "UTF-8");

        ParserState state = ParserState.ParserScanningToRecord;
        Map<String, String> fieldMap = new HashMap<>();
        List<StackFrame> stackFrames = new ArrayList<>();
        LogException logException = null;

        String exceptionType = null, exceptionMessage = null;
        String frameClass = null, frameMethod = null;
        int frameLine = -1;

        while (reader.hasNext()) {
            int event = reader.next();

            switch (state) {
                case ParserScanningToRecord -> {
                    if (event == XMLStreamConstants.START_ELEMENT &&
                        reader.getLocalName().equals("record")) {
                        fieldMap.clear();
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
                            fieldMap.put(tag, reader.getElementText());
                        }
                    } else if (event == XMLStreamConstants.END_ELEMENT &&
                               reader.getLocalName().equals("record")) {
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
                            case "exceptionType" -> exceptionType = reader.getElementText();
                            case "message" -> exceptionMessage = reader.getElementText();
                            case "frames" -> state = ParserState.ParserLoadingFrames;
                        }
                    } else if (event == XMLStreamConstants.END_ELEMENT &&
                               reader.getLocalName().equals("exception")) {
                        logException = new LogException(exceptionType, exceptionMessage, new ArrayList<>(stackFrames));
                        state = ParserState.ParserLoadingFields;
                    }
                }

                case ParserLoadingFrames -> {
                    if (event == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals("frame")) {
                        state = ParserState.ParserLoadingFrame;
                    }
                }

                case ParserLoadingFrame -> {
                    if (event == XMLStreamConstants.START_ELEMENT) {
                        switch (reader.getLocalName()) {
                            case "class" -> frameClass = reader.getElementText();
                            case "method" -> frameMethod = reader.getElementText();
                            case "line" -> frameLine = Integer.parseInt(reader.getElementText());
                        }
                    } else if (event == XMLStreamConstants.END_ELEMENT &&
                               reader.getLocalName().equals("frame")) {
                        stackFrames.add(new StackFrame(frameClass, frameMethod, frameLine));
                        state = ParserState.ParserLoadingFrames;
                    }
                }
            }
        }

        throw new XMLStreamException("No <record> found in stream.");
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
}
