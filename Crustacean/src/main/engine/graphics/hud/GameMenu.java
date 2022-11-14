package main.engine.graphics.hud;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.nuklear.Nuklear.*;

import org.lwjgl.PointerBuffer;
import org.lwjgl.nuklear.*;
import org.lwjgl.system.MemoryStack;

import main.engine.Window;
import org.lwjgl.system.MemoryUtil;
import org.tinylog.Logger;

import java.nio.ByteBuffer;

public class GameMenu implements NKHudElement {
	
	private final Window window;

	private final int width;
	private final int height;

	private boolean shouldHide;

	public enum Level {SPONZA, NEWTONDEMO};
	private Level currentLevel;
	
	public GameMenu(Window window) {
		this.window = window;
		this.shouldHide = true;
		currentLevel = Level.SPONZA;
		width = 300;
		height = 150;
	}

	public void cleanup() {
	}

	public Level getLevel() {
		return currentLevel;
	}

	@Override
	public void layout(NkContext ctx) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			NkRect rect = NkRect.malloc(stack);
			if (nk_begin(ctx, "Menu", nk_rect(width, height, window.getWidth() - (2 * width), window.getHeight() - (2 * height), rect),
					NK_WINDOW_TITLE)) { // | NK_WINDOW_HIDDEN
				nk_window_show_if(ctx, "Menu", NK_HIDDEN, shouldHide);

				nk_menubar_begin(ctx);
				nk_layout_row_begin(ctx, NK_STATIC, 25, 3);
				nk_layout_row_push(ctx, 40);
				NkVec2 vec2 = NkVec2.malloc(stack);
				vec2.x(120);
				vec2.y(200);
				if (nk_menu_begin_label(ctx, "Levels", NK_TEXT_LEFT, vec2)) {
					nk_layout_row_dynamic(ctx, 25, 1);
					if (nk_menu_item_label(ctx, "Sponza", NK_TEXT_LEFT)) {
						currentLevel = Level.SPONZA;
						Logger.debug("Current Level: {}", currentLevel);
					}
					if (nk_menu_item_label(ctx, "Newton Demo", NK_TEXT_LEFT)) {
						currentLevel = Level.NEWTONDEMO;
						Logger.debug("Current Level: {}", currentLevel);
					}
					nk_menu_end(ctx);
				}
				nk_layout_row_push(ctx, 70);
				if (nk_button_label(ctx, "Reset")) {
					Logger.debug("Reset Level");
				}
				if (nk_button_label(ctx, "Exit")) {
					Logger.debug("End Game");
					glfwSetWindowShouldClose(window.getWindowHandle(), true);
				}
				nk_menubar_end(ctx);
			}
			nk_end(ctx);
		}
	}
	
	public void showWindow(boolean show_window) {
		this.shouldHide = show_window;
	}
}
