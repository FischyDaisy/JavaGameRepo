package main.engine.input;

public class EngineInput {

    public final KeyboardInput keyboardInput;
    public final MouseInput mouseInput;

    public EngineInput(long windowHandle) {
        keyboardInput = new KeyboardInput(windowHandle);
        mouseInput = new MouseInput(windowHandle);
    }
}
