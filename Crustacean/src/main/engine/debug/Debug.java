package main.engine.debug;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.foreign.*;
import java.util.List;

public class Debug {

    private static final DebugOutputStream dos = new DebugOutputStream();
    private static PrintStream out, err, debugStream;

    public static void init() {
        out = System.out;
        err = System.err;
        debugStream = new PrintStream(dos);
        System.setOut(debugStream);
        System.setErr(debugStream);
    }

    public static void cleanup() {
        System.setOut(out);
        System.setErr(err);
    }
    public static void log(String entry) {
        dos.logString(entry);
    }

    public static MemorySegment getMemorySegment() {
        return dos.getSegment();
    }
}
