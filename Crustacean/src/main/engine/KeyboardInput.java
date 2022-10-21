package main.engine;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.nuklear.Nuklear.*;

public class KeyboardInput {

    public static final int KEY_STATE_LENGTH = 349;
    private final long windowHandle;
    private final int[] charList = new int[NK_INPUT_MAX];
    private final int[] keyState;
    private final int[] frameCount;
    private final boolean[] initPress;
    private int charPtr;
    private int holdThreshold;

    public KeyboardInput(long windowHandle) {
        this.windowHandle = windowHandle;
        charPtr = 0;
        holdThreshold = 5;
        keyState = new int[KEY_STATE_LENGTH];
        frameCount = new int[KEY_STATE_LENGTH];
        initPress = new boolean[KEY_STATE_LENGTH];
        glfwSetCharCallback(windowHandle, (window, codepoint) -> {
            charList[charPtr++] = codepoint;
        });
    }

    public int[] getCharList() {
        return charList;
    }

    public int charListLength() {
        return charPtr;
    }

    public void clearCharList() {
        charPtr = 0;
    }

    public int getHoldThreshold() {
        return holdThreshold;
    }

    public void setHoldThreshold(int threshold) {
        holdThreshold = threshold;
    }

    public void input() {
        for (int i = 32; i < KEY_STATE_LENGTH; i++) {
            keyState[i] = glfwGetKey(windowHandle, i);
            if (keyState[i] == GLFW_PRESS) {
                if (frameCount[i] == 0) {
                    initPress[i] = true;
                }
                frameCount[i]++;
                continue;
            }
            frameCount[i] = 0;
        }
    }

    /**
     * Checks if the specified key is pressed down regardless of when it was pressed.
     * @param key
     * @return true if key is down. false otherwise
     */
    public boolean isKeyPressed(int key) {
        return keyState[key] == GLFW_PRESS;
    }

    public boolean isKeyPressedOnce(int key) {
        if (keyState[key] == GLFW_PRESS && initPress[key] == true) {
            initPress[key] = false;
            return true;
        }
        return false;
    }

    /**
     * Checks if key is pressed after being held for a certain amount of loops
     * @param key
     * @return true if key has been held longer than hold threshold
     */
    public boolean isKeyHeld(int key) {
        return frameCount[key] > holdThreshold;
    }

    /**
     * Checks if the key is released regardless of when it was released.
     * @param key
     * @return true if key is up.
     */
    public boolean isReleased(int key) {
        return keyState[key] == GLFW_RELEASE;
    }

    public int getFrameCount(int key) {
        return frameCount[key];
    }
}
