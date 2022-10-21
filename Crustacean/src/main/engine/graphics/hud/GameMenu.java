package main.engine.graphics.hud;

import static org.lwjgl.nuklear.Nuklear.*;

import org.lwjgl.nuklear.*;
import org.lwjgl.system.MemoryStack;

import main.engine.Window;

public class GameMenu implements NKHudElement {
	
	private final Window window;

	private boolean shouldHide;
	
	public GameMenu(Window window) {
		this.window = window;
		this.shouldHide = true;
	}

	@Override
	public void layout(NkContext ctx) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			NkRect rect = NkRect.malloc(stack);
			if (nk_begin(ctx, "Menu", nk_rect(50, 50, window.getWidth() - 100, window.getHeight() - 100, rect),
					NK_WINDOW_TITLE)) { // | NK_WINDOW_HIDDEN
				nk_window_show_if(ctx, "Menu", NK_HIDDEN, shouldHide);
			}
			nk_end(ctx);
		}
	}
	
	public void showWindow(boolean show_window) {
		this.shouldHide = show_window;
	}
}
