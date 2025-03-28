package theider.log4jxmlview.logrecord;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class CountingInputStreamTest {

    @Test
    void testCountingInputStreamSingleByteRead() throws IOException {
        byte[] data = "Hello, World!".getBytes(); // 13 bytes
        CountingInputStream cis = new CountingInputStream(new ByteArrayInputStream(data));

        for (int i = 0; i < data.length; i++) {
            int b = cis.read();
            assertEquals(data[i], (byte) b);
            assertEquals(i + 1, cis.getCount(), "Count after reading " + (i + 1) + " bytes should match");
        }

        assertEquals(-1, cis.read()); // EOF
        assertEquals(data.length, cis.getCount(), "Final byte count should match total input length");
    }

    @Test
    void testCountingInputStreamBufferRead() throws IOException {
        byte[] data = "This is a test".getBytes(); // 14 bytes
        CountingInputStream cis = new CountingInputStream(new ByteArrayInputStream(data));

        byte[] buffer = new byte[5];
        int totalRead = 0;
        int read;
        while ((read = cis.read(buffer)) != -1) {
            totalRead += read;
        }

        assertEquals(data.length, totalRead);
        assertEquals(data.length, cis.getCount(), "Byte count should match total bytes read");
    }

    @Test
    void testZeroBytesReadDoesNotIncrementCount() throws IOException {
        byte[] data = "abc".getBytes();
        CountingInputStream cis = new CountingInputStream(new ByteArrayInputStream(data));

        byte[] buffer = new byte[10];
        int read = cis.read(buffer, 0, 0);
        assertEquals(0, read);
        assertEquals(0, cis.getCount(), "Count should not change for zero-byte read");
    }
}
