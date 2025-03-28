package theider.log4jxmlview.logrecord;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogRecordIndexer {

    private static final Logger logger = LoggerFactory.getLogger(LogRecordIndexer.class);
    
    private static final byte[] START_TAG = "<record".getBytes(StandardCharsets.UTF_8);
    private static final byte[] END_TAG = "</record>".getBytes(StandardCharsets.UTF_8);

    private static final int LONG_ARRAY_PAGESIZE = 4; // testing. should be a big value
    
    public LogRecordOffsetIndex indexRecords(InputStream in) throws IOException {
        long[] indexArray = new long[LONG_ARRAY_PAGESIZE];
        int recordCount = 0;        
        logger.debug("loading index from stream...");
        CountingInputStream cis = new CountingInputStream(new BufferedInputStream(in));

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        boolean insideRecord = false;
        long recordStart = -1;

        int b;
        while ((b = cis.read()) != -1) {
            buffer.write(b);
            byte[] buf = buffer.toByteArray();

            if (!insideRecord && endsWith(buf, START_TAG)) {
                insideRecord = true;
                recordStart = cis.getCount() - START_TAG.length;
                buffer.reset(); // reset for fresh tag scanning
            } else if (insideRecord && endsWith(buf, END_TAG)) {
                // Should we expand the output array?
                if(recordCount == indexArray.length) {
                    indexArray = Arrays.copyOf(indexArray, indexArray.length + LONG_ARRAY_PAGESIZE);
                }                
                indexArray[recordCount] = recordStart;
                recordCount++;
                insideRecord = false;
                recordStart = -1;
                buffer.reset();
            } else if (buffer.size() > 1024) {
                // don't let buffer grow forever
                buffer.reset();
            }
        }
        if(indexArray.length >= recordCount) {
            indexArray = Arrays.copyOf(indexArray, recordCount);
        }
        logger.debug("scanned index of {} records size {} bytes.", recordCount, indexArray.length * (Long.SIZE / 8));
        return new LogRecordOffsetIndex(cis.getCount(), indexArray);
    }

    private boolean endsWith(byte[] buffer, byte[] suffix) {
        if (buffer.length < suffix.length) return false;
        for (int i = 0; i < suffix.length; i++) {
            if (buffer[buffer.length - suffix.length + i] != suffix[i]) return false;
        }
        return true;
    }
}
