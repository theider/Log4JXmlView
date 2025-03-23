package theider.log4jxmlview.logrecord;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class LogRecordFactory {

    public static LogRecord fromElement(Element recordElement) {
        return new LogRecord(
            getElementText(recordElement, "timestamp"),
            Long.parseLong(getElementText(recordElement, "sequence")),
            getElementText(recordElement, "loggerClassName"),
            getElementText(recordElement, "loggerName"),
            getElementText(recordElement, "level"),
            getElementText(recordElement, "message"),
            getElementText(recordElement, "threadName"),
            Long.parseLong(getElementText(recordElement, "threadId")),
            getElementText(recordElement, "mdc"),
            getElementText(recordElement, "ndc"),
            getElementText(recordElement, "hostName"),
            getElementText(recordElement, "processName"),
            Long.parseLong(getElementText(recordElement, "processId")),
            getExceptionFromElement(recordElement)
        );
    }

    private static String getElementText(Element parent, String tag) {
        NodeList list = parent.getElementsByTagName(tag);
        return list.getLength() > 0 ? list.item(0).getTextContent() : "";
    }

    private static LogException getExceptionFromElement(Element recordElement) {
        NodeList exceptionNodes = recordElement.getElementsByTagName("exception");
        if (exceptionNodes.getLength() == 0) return null;

        Element exEl = (Element) exceptionNodes.item(0);
        String exType = getElementText(exEl, "exceptionType");
        String exMessage = getElementText(exEl, "message");

        List<StackFrame> frames = new ArrayList<>();
        NodeList frameNodes = exEl.getElementsByTagName("frame");
        for (int i = 0; i < frameNodes.getLength(); i++) {
            Element frameEl = (Element) frameNodes.item(i);
            frames.add(new StackFrame(
                getElementText(frameEl, "class"),
                getElementText(frameEl, "method"),
                Integer.parseInt(getElementText(frameEl, "line"))
            ));
        }

        return new LogException(exType, exMessage, frames);
    }
}
