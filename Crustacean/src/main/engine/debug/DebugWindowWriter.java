package main.engine.debug;

import org.tinylog.core.LogEntry;
import org.tinylog.core.LogEntryValue;
import org.tinylog.writers.AbstractFormatPatternWriter;
import org.tinylog.writers.Writer;

import java.util.Collection;
import java.util.Map;

public class DebugWindowWriter extends AbstractFormatPatternWriter {

    public DebugWindowWriter(Map<String, String> properties) {
        super(properties);
    }

    @Override
    public Collection<LogEntryValue> getRequiredLogEntryValues() {
        Collection<LogEntryValue> logEntryValues = super.getRequiredLogEntryValues();
        logEntryValues.add(LogEntryValue.LEVEL);
        return logEntryValues;
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
