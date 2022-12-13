package main.engine.debug;

import java.lang.foreign.*;

public class Debug {

    private static MemorySession debugSession = MemorySession.openShared();

    public static void log(String entry) {
    }
}
