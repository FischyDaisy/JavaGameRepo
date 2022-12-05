package main.engine.debug;

import org.tinylog.core.LogEntry;
import org.tinylog.core.LogEntryValue;
import org.tinylog.writers.Writer;

import java.util.Collection;

public class DebugWriter implements Writer {
    @Override
    public Collection<LogEntryValue> getRequiredLogEntryValues() {
        return null;
    }

    @Override
    public void write(LogEntry logEntry) throws Exception {

    }

    @Override
    public void flush() throws Exception {

    }

    @Override
    public void close() throws Exception {

    }
}
