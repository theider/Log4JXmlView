package theider.log4jxmlview;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public record LogRecord(
    String timestamp,
    long sequence,
    String loggerClassName,
    String loggerName,
    String level,
    String message,
    String threadName,
    long threadId,
    String mdc,
    String ndc,
    String hostName,
    String processName,
    long processId
) {
    @Override
    public String toString() {
        return "[" + timestamp + "] [" + level + "] [" + threadName + "] " + message;
    }

    public static LogRecord fromElement(Element recordElement) {
        return new LogRecord(
            getText(recordElement, "timestamp"),
            Long.parseLong(getText(recordElement, "sequence")),
            getText(recordElement, "loggerClassName"),
            getText(recordElement, "loggerName"),
            getText(recordElement, "level"),
            getText(recordElement, "message"),
            getText(recordElement, "threadName"),
            Long.parseLong(getText(recordElement, "threadId")),
            getText(recordElement, "mdc"),
            getText(recordElement, "ndc"),
            getText(recordElement, "hostName"),
            getText(recordElement, "processName"),
            Long.parseLong(getText(recordElement, "processId"))
        );
    }

    private static String getText(Element parent, String tag) {
        NodeList list = parent.getElementsByTagName(tag);
        return list.getLength() > 0 ? list.item(0).getTextContent() : "";
    }
}
