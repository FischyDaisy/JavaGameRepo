package main.game.hud;

import main.engine.Window;
import main.engine.graphics.ui.NKHudElement;
import org.lwjgl.nuklear.*;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.nuklear.Nuklear.*;

public class TransparentWindow implements NKHudElement {

    public final Window window;
    public final int width, height;
    public final String message;

    public TransparentWindow(Window window) {
        this.window = window;
        width = 200;
        height = 50;
        message = "Press ESC to open menu";
    }

    @Override
    public void layout(NkContext ctx) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            NkRect rect = NkRect.malloc(stack);
            NkStyle style = ctx.style();
            NkColor background = NkColor.malloc(stack);
            NkStyleItem styleItem = NkStyleItem.malloc(stack);
            nk_style_push_color(ctx, style.window().background(), nk_rgba(0, 0,0, 0, background));
            nk_style_push_style_item(ctx, style.window().fixed_background(), nk_style_item_color(nk_rgba(0, 0,0, 0, background), styleItem));
            if (nk_begin(ctx, "Message", nk_rect(0, 0, width, height, rect), 0)) {
                nk_window_show_if(ctx, "Message", NK_SHOWN, true);
                nk_layout_row_dynamic(ctx, 25, 1);
                nk_label(ctx, message, NK_TEXT_LEFT);
            }
            nk_end(ctx);
            nk_style_pop_color(ctx);
            nk_style_pop_style_item(ctx);
        }
    }
}
