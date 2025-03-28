package theider.log4jxmlview.logrecord;

@FunctionalInterface
public interface ILogRecordIndexProgressListener {
    void onProgress(long bytesRead, long totalBytes);
}
