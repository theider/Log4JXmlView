package theider.log4jxmlview.logrecord;

public record StackFrame(
    String className,
    String methodName,
    int lineNumber
) {}
