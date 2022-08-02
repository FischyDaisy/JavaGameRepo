package main.engine.graphics.hud;

import static org.lwjgl.nuklear.Nuklear.*;

import org.lwjgl.nuklear.*;
import org.lwjgl.system.MemoryStack;

import main.engine.Window;

public class GameMenu implements NKHudElement {
	
	private final Window window;
	
	public GameMenu(Window window) {
		this.window = window;
	}

	@Override
	public void layout(NkContext ctx) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			NkRect rect = NkRect.malloc(stack);
			if (nk_begin(ctx, "Menu", nk_rect(50, 50, window.getWidth() - 50, window.getHeight() - 50, rect), 
					NK_WINDOW_TITLE | NK_WINDOW_HIDDEN)) {
				
			}
			nk_end(ctx);
		}
	}
	
	public void showWindow(NkContext ctx, boolean show_window) {
		if (show_window) {
			nk_window_show_if(ctx, "Menu", NK_SHOWN, true);
		} else {
			nk_window_show_if(ctx, "Menu", NK_HIDDEN, true);
		}
	}
}
