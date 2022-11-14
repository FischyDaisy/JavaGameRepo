package main.engine;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import org.lwjgl.glfw.*;
import org.lwjgl.system.MemoryUtil;

import org.joml.Matrix4f;

import static org.lwjgl.glfw.GLFWVulkan.glfwVulkanSupported;

public class Window {

    private final String title;
    
    private final EngineProperties props = EngineProperties.INSTANCE;

    private int width;

    private int height;

    private long windowHandle;

    private boolean resized;

    private boolean vSync;
    
    private MouseInput mouseInput;

    private KeyboardInput keyboardInput;
    
    private WindowOptions opts;
    
    private Matrix4f projectionMatrix;

    public Window(String title, int width, int height, boolean vSync, WindowOptions opts) {
        this.title = title;
        this.width = width;
        this.height = height;
        this.vSync = vSync;
        this.resized = false;
        this.opts = opts;
        projectionMatrix = new Matrix4f();
    }

    public void init(GLFWKeyCallbackI keyCallback) {
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        if (!glfwVulkanSupported()) {
            throw new IllegalStateException("Cannot find a compatible Vulkan installable client driver (ICD)");
        }

        GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        width = vidMode.width();
        height = vidMode.height();
        //setResized(true);

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
        glfwWindowHint(GLFW_MAXIMIZED, GLFW_FALSE);

        // Create the window
        windowHandle = glfwCreateWindow(width, height, title + ": Vulkan", MemoryUtil.NULL, MemoryUtil.NULL);
        if (windowHandle == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        glfwSetFramebufferSizeCallback(windowHandle, (window, width, height) -> resize(width, height));

        updateProjectionMatrix();
        mouseInput = new MouseInput(windowHandle);
        keyboardInput = new KeyboardInput(windowHandle);
    }
    
    public long getWindowHandle() {
        return windowHandle;
    }
    
    public int getKey(int keyCode) {
    	return glfwGetKey(windowHandle, keyCode);
    }

    public KeyboardInput getKeyboardInput() {
        return keyboardInput;
    }

    public boolean windowShouldClose() {
        return glfwWindowShouldClose(windowHandle);
    }
    
    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    public Matrix4f updateProjectionMatrix() {
        float aspectRatio = (float) width / (float) height;
        return projectionMatrix.setPerspective(props.getFOV(), aspectRatio, props.getZNear(), props.getZFar());
    }

    public String getTitle() {
        return title;
    }
    
    public void setWindowTitle(String title) {
        glfwSetWindowTitle(windowHandle, title);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
    
    public MouseInput getMouseInput() {
        return mouseInput;
    }
    
    public void pollEvents() {
        glfwPollEvents();
        mouseInput.input();
        keyboardInput.input();
    }

    public boolean isResized() {
        return resized;
    }

    public void setResized(boolean resized) {
        this.resized = resized;
    }
    
    public void resize(int width, int height) {
        resized = true;
        this.width = width;
        this.height = height;
    }

    public boolean isvSync() {
        return vSync;
    }

    public void setvSync(boolean vSync) {
        this.vSync = vSync;
    }
    
    public void cleanup() {
        glfwFreeCallbacks(windowHandle);
        glfwDestroyWindow(windowHandle);
        glfwTerminate();
    }
    
    public WindowOptions getOptions() {
        return opts;
    }
    
    public static class WindowOptions {

        public boolean cullFace;

        public boolean showTriangles;
        
        public boolean showFps;

        public boolean useVulkan;
        
        public boolean compatibleProfile;
        
        public boolean antialiasing;

        public boolean frustumCulling;    
    }
}
