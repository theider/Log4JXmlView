package theider.log4jxmlview.logrecord;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class LogDataWrapperStream extends InputStream {
    private final byte[] prefix = "<log>".getBytes();
    private final byte[] suffix = "</log>".getBytes();

    private final ByteArrayInputStream prefixStream = new ByteArrayInputStream(prefix);
    private final ByteArrayInputStream suffixStream = new ByteArrayInputStream(suffix);
    private final InputStream originalStream;

    public LogDataWrapperStream(InputStream originalStream) {
        this.originalStream = originalStream;
    }

    @Override
    public int read() throws IOException {
        if (prefixStream.available() > 0) {
            return prefixStream.read();
        } else if (originalStream.available() > 0) {
            return originalStream.read();
        } else {
            return suffixStream.read();
        }
    }

    @Override
    public void close() throws IOException {
        prefixStream.close();
        suffixStream.close();
        originalStream.close();
    }
}
