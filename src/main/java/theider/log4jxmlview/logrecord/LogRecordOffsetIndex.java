package theider.log4jxmlview.logrecord;

public record LogRecordOffsetIndex(long totalSourceFileSizeBytes, long[] offsetIndexArray) {

    public int getRecordCount() {
        return offsetIndexArray.length;
    }

    public long getRecordOffset(int index) {
        if ((index < 0) || (index >= offsetIndexArray.length)) {
            throw new RuntimeException("Record offset " + index + " out of bounds");
        } else {
            return offsetIndexArray[index];
        }
    }

    public long getRecordSize(int index) {
        if ((index < 0) || (index >= offsetIndexArray.length)) {
            throw new RuntimeException("Record offset " + index + " out of bounds");
        } else if (index == (offsetIndexArray.length - 1)) {
            return totalSourceFileSizeBytes - offsetIndexArray[index];
        } else {
            return offsetIndexArray[index + 1] - offsetIndexArray[index];
        }
    }

}
