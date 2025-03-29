package theider.log4jxmlview.logrecord.model;

import java.util.List;

public record LogException(
    String exceptionType,
    String message,
    List<StackFrame> frames,
    LogException causedBy // nullable
) {}
