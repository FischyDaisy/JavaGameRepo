package main.game.hud;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.nuklear.Nuklear.*;

import crab.newton.NewtonWorld;
import dev.dominion.ecs.api.Dominion;
import main.engine.graphics.hud.NKHudElement;
import main.engine.graphics.vulkan.VKRenderer;
import main.engine.physics.Physics;
import main.engine.scene.Level;
import main.game.NewtonDemo;
import main.game.Sponza;
import org.lwjgl.nuklear.*;
import org.lwjgl.system.MemoryStack;

import main.engine.Window;
import org.tinylog.Logger;

import java.lang.foreign.MemorySession;

public class GameMenu implements NKHudElement {
	
	private final Window window;
	private final Dominion dominion;
	private final VKRenderer renderer;
	private final Physics physics;
	private final MemorySession session;

	private final int width;
	private final int height;

	private boolean shouldHide;

	private final Level[] levels;
	private int currentLevel;
	
	public GameMenu(Window window, Dominion dominion, VKRenderer renderer, Physics physics, MemorySession session) throws Exception {
		this.window = window;
		this.dominion = dominion;
		this.renderer = renderer;
		this.physics = physics;
		this.session = session;
		this.shouldHide = true;
		levels = new Level[] {new Sponza(), new NewtonDemo(physics, session)};
		currentLevel = 0;
		width = 300;
		height = 150;
	}

	public void cleanup() {
	}

	public Level getLevel() {
		return levels[currentLevel];
	}

	public int getCurrentLevel() {
		return currentLevel;
	}

	@Override
	public void layout(NkContext ctx) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			NkRect rect = NkRect.malloc(stack);
			if (!shouldHide) {
				if (nk_begin(ctx, "Menu", nk_rect(width, height, window.getWidth() - (2 * width), window.getHeight() - (2 * height), rect),
						NK_WINDOW_TITLE)) { // | NK_WINDOW_HIDDEN
					//nk_window_show_if(ctx, "Menu", NK_HIDDEN, shouldHide);

					nk_menubar_begin(ctx);
					nk_layout_row_begin(ctx, NK_STATIC, 25, 3);
					nk_layout_row_push(ctx, 40);
					NkVec2 vec2 = NkVec2.malloc(stack);
					vec2.x(120);
					vec2.y(200);
					if (nk_menu_begin_label(ctx, "Levels", NK_TEXT_LEFT, vec2)) {
						nk_layout_row_dynamic(ctx, 25, 1);
						if (nk_menu_item_label(ctx, "Sponza", NK_TEXT_LEFT)) {
							currentLevel = 0;
							shouldHide = true;
							glfwSetInputMode(window.getWindowHandle(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);
							Logger.debug("Current Level: {}", currentLevel);
						}
						if (nk_menu_item_label(ctx, "Newton Demo", NK_TEXT_LEFT)) {
							currentLevel = 1;
							shouldHide = true;
							glfwSetInputMode(window.getWindowHandle(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);
							Logger.debug("Current Level: {}", currentLevel);
						}
					}
					nk_menu_end(ctx);
					nk_layout_row_push(ctx, 70);
					if (nk_button_label(ctx, "Reset")) {
						Logger.debug("Reset Level");
						levels[currentLevel].reset(dominion, renderer, physics, session);
					}
					if (nk_button_label(ctx, "Exit")) {
						Logger.debug("End Game");
						glfwSetWindowShouldClose(window.getWindowHandle(), true);
					}
					nk_menubar_end(ctx);

					nk_layout_row_dynamic(ctx, 25, 1);
					nk_label(ctx, "Controls:", NK_TEXT_CENTERED);
					nk_label(ctx, "W - Forwards | A - Left | S - Backwards | D - Right", NK_TEXT_LEFT);
					nk_label(ctx, "X - Up | Z - Down", NK_TEXT_LEFT);
					nk_label(ctx, "Left and Right arrows adjust light angle", NK_TEXT_LEFT);
					switch (currentLevel) {
						case 0 -> {
							nk_label(ctx, "Press SPACE to toggle animations", NK_TEXT_LEFT);
						}
						case 1 -> {
							nk_label(ctx, "Press F to toggle Physics", NK_TEXT_LEFT);
						}
					}
				}
				nk_end(ctx);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void hideWindow(boolean shouldHide) {
		this.shouldHide = shouldHide;
		if (shouldHide) {
			glfwSetInputMode(window.getWindowHandle(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);
		} else {
			glfwSetInputMode(window.getWindowHandle(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);
		}
	}

	public boolean isHidden() {
		return shouldHide;
	}
}
