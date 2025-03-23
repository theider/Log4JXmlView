package theider.log4jxmlview.logrecord;

public class LogFileRecordIndexEntry {

    private final int index;

    private final long offset;

    private final int sizeBytes;

    public LogFileRecordIndexEntry(int index, long offset, int sizeBytes) {
        this.index = index;
        this.offset = offset;
        this.sizeBytes = sizeBytes;
    }

    public int getIndex() {
        return index;
    }

    public long getOffset() {
        return offset;
    }

    public int getSizeBytes() {
        return sizeBytes;
    }

}
