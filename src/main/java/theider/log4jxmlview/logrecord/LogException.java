package theider.log4jxmlview.logrecord;

import java.util.List;

public record LogException(
    String exceptionType,
    String message,
    List<StackFrame> frames
) {}
