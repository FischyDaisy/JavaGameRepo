package main.game.hud;

import main.engine.graphics.hud.NKHudElement;
import org.lwjgl.nuklear.*;
import org.lwjgl.system.MemoryStack;
import org.tinylog.Logger;
import org.tinylog.core.LogEntry;
import org.tinylog.core.LogEntryValue;
import org.tinylog.writers.Writer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import static org.lwjgl.nuklear.Nuklear.*;

public class DebugWindow implements NKHudElement, Writer {

    private final int width;
    private final int height;

    public DebugWindow(Map<String, String> properties) {
        width = 50;
        height = 50;
    }

    @Override
    public Collection<LogEntryValue> getRequiredLogEntryValues() {
        return null;
    }

    @Override
    public void write(LogEntry logEntry) throws Exception {
        System.out.println();
    }

    @Override
    public void flush() throws IOException {
        System.out.flush();
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public void layout(NkContext ctx) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            NkRect rect = NkRect.malloc(stack);
            //if (nk_begin(ctx, "debug", nk_rect()))
        }
    }
}
