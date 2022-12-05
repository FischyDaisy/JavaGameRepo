package main.engine.debug;

import java.io.BufferedWriter;
import java.util.concurrent.locks.StampedLock;

public class DebugStream {

    private final StampedLock lock = new StampedLock();

    //private final BufferedWriter textOut;

    protected DebugStream() {

    }
}
