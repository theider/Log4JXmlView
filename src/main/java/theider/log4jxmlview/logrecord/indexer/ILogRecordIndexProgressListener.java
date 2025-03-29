package theider.log4jxmlview.logrecord.indexer;

@FunctionalInterface
public interface ILogRecordIndexProgressListener {
    void onProgress(long bytesRead, long totalBytes);
}
