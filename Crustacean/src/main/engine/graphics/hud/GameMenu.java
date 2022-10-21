package main.engine.graphics.hud;

import static org.lwjgl.nuklear.Nuklear.*;

import org.lwjgl.nuklear.*;
import org.lwjgl.system.MemoryStack;

import main.engine.Window;

public class GameMenu implements NKHudElement {
	
	private final Window window;

	private boolean shouldShow;
	
	public GameMenu(Window window) {
		this.window = window;
		this.shouldShow = false;
	}

	@Override
	public void layout(NkContext ctx) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			NkRect rect = NkRect.malloc(stack);
			if (nk_begin(ctx, "Menu", nk_rect(50, 50, window.getWidth() - 50, window.getHeight() - 50, rect), 
					NK_WINDOW_TITLE)) { // | NK_WINDOW_HIDDEN
				nk_window_show_if(ctx, "Menu", NK_HIDDEN, shouldShow);
			}
			nk_end(ctx);
		}
	}
	
	public void showWindow(boolean show_window) {
		this.shouldShow = show_window;
	}
}
