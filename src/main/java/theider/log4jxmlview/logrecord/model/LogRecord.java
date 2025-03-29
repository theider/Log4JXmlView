package theider.log4jxmlview.logrecord.model;

public record LogRecord(
        String timestamp,
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
