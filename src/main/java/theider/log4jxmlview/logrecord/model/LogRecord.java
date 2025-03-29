package theider.log4jxmlview.logrecord.model;

import java.time.Instant;

public record LogRecord(
        Instant timestamp,
        long sequence,
        String loggerClassName,
        String loggerName,
        String level,
        String message,
        String threadName,
        long threadId,
        String hostName,
        String processName,
        long processId,
        LogException exception
) {
    @Override
    public String toString() {
        return "[" + timestamp + "] [" + level + "] msg:" + message;
    }
}
