package theider.log4jxmlview.logrecord.model;

public record StackFrame(
    String className,
    String methodName,
    int lineNumber
) {}
