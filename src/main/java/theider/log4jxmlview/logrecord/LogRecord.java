package theider.log4jxmlview.logrecord;

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
        // DateTimeFormatter.ISO_INSTANT.format
        
) {
    @Override
    public String toString() {
        return "[" + timestamp + "] [" + level + "] [" + threadName + "] " + message;
    }
}