package main.engine.debug;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.foreign.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static java.lang.foreign.ValueLayout.JAVA_BYTE;

public class DebugOutputStream extends OutputStream {

    private static final long chunkSize = 420;
    private MemorySession session;
    private MemorySegment segment;
    private long end;

    public DebugOutputStream() {
        session = MemorySession.openConfined();
        segment = session.allocate(chunkSize);
        end = 0;
    }

    @Override
    public void write(int b) throws IOException {
        long remainder = segment.byteSize() - end;
        if (remainder <= 1) {
            long size = segment.byteSize() + chunkSize;
            MemorySession session = MemorySession.openConfined();
            MemorySegment segment = session.allocate(size);
            MemorySegment.copy(this.segment, 0, segment, 0, this.segment.byteSize());
            this.segment = segment;
            this.session.close();
            this.session = session;
        }
        segment.set(JAVA_BYTE, end, (byte)b);
    }

    public void logString(String str) {
        Objects.requireNonNull(str);
        byte[] strBytes = str.getBytes(StandardCharsets.UTF_8);
        long remainder = segment.byteSize() - end;
        if (remainder < strBytes.length + 1) {
            long size = segment.byteSize() + strBytes.length + 1 + chunkSize;
            MemorySession session = MemorySession.openConfined();
            MemorySegment segment = session.allocate(size);
            MemorySegment.copy(this.segment, 0, segment, 0, this.segment.byteSize());
            this.segment = segment;
            this.session.close();
            this.session = session;
        }
        MemorySegment strSegment = MemorySegment.ofArray(strBytes);
        MemorySegment.copy(strSegment, 0, segment, end, strSegment.byteSize());
        end = end + strSegment.byteSize();
        segment.set(JAVA_BYTE, end, (byte)0);
    }

    public MemorySegment getSegment() {
        return segment;
    }
}
