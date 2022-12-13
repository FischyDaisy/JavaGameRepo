package main.game.hud;

import main.engine.Window;
import main.engine.graphics.hud.NKHudElement;
import org.lwjgl.nuklear.*;
import org.lwjgl.system.MemoryStack;
import org.tinylog.Logger;
import org.tinylog.core.LogEntry;
import org.tinylog.core.LogEntryValue;
import org.tinylog.writers.Writer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.Collection;
import java.util.Map;

import static org.lwjgl.nuklear.Nuklear.*;

public class DebugWindow implements NKHudElement {

    private final int width;
    private final int height;
    private final Window window;
    private final NkPluginFilterI filter;

    public DebugWindow(Window window) {
        width = 50;
        height = 50;
        this.window = window;
        filter = NkPluginFilter.create(Nuklear::nnk_filter_default);
    }

    @Override
    public void layout(NkContext ctx) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            NkRect rect = NkRect.malloc(stack);
            int w = window.getWidth(), h = window.getHeight();
            if (nk_begin(ctx, "debug", nk_rect(w - width, h - height, width, height, rect),
                    NK_WINDOW_MOVABLE | NK_WINDOW_MINIMIZABLE)) {
                FloatBuffer buffer = stack.floats(120, 150);
                nk_layout_row(ctx, NK_STATIC, 25, buffer);
                nk_label(ctx, "fps:", NK_TEXT_LEFT);
                //nk_edit_string(ctx, NK_EDIT_SIMPLE, "", 20, 64, filter);
            }
        }
    }
}
