package main.engine;

import main.engine.graphics.IRenderer;
import main.engine.graphics.opengl.GLRenderer;
import main.engine.graphics.vulkan.VKRenderer;
import main.engine.items.GameItem;
import main.engine.utility.Timer;

public class GameEngine implements Runnable {
    
    private boolean running;
    
    private Scene scene;
    
    private static final EngineProperties engineProperties = EngineProperties.getInstance();

    private final Window window;

    private final IGameLogic gameLogic;
    
    private final IRenderer renderer;
    
    private double lastFps;
    
    private int fps;
    
    private long initialTime;
    
    private String windowTitle;

    public GameEngine(String windowTitle, boolean vSync, Window.WindowOptions opts, IGameLogic gameLogic) throws Exception {
        this(windowTitle, 0, 0, vSync, opts, gameLogic);
    }
    
    public GameEngine(String windowTitle, Window.WindowOptions opts, IGameLogic gameLogic) throws Exception {
        this(windowTitle, 0, 0, engineProperties.isvSync(), opts, gameLogic);
    }
    
    public GameEngine(String windowTitle, int width, int height, boolean vSync, Window.WindowOptions opts, IGameLogic gameLogic) throws Exception {
    	this.windowTitle = windowTitle;
    	opts.useVulkan = engineProperties.useVulkan();
    	window = new Window(windowTitle, width, height, vSync, opts);
    	window.init(null);
        this.gameLogic = gameLogic;
        scene = new Scene();
        this.renderer = opts.useVulkan == false ? new GLRenderer() : new VKRenderer(window, scene);
        gameLogic.init(window, scene, renderer);
        lastFps = System.nanoTime();
        fps = 0;
    }

    @Override
    public void run() {
        initialTime = System.nanoTime();
        double timeU = 1000000000d / engineProperties.getUps();
        double deltaU = 0;

        long updateTime = initialTime;
        while (running && !window.windowShouldClose()) {

            window.pollEvents();

            long currentTime = System.nanoTime();
            deltaU += (currentTime - initialTime) / timeU;
            initialTime = currentTime;

            gameLogic.input(window, scene, currentTime - updateTime);
            if (deltaU >= 1) {
                gameLogic.update(timeU, window.getMouseInput());
                updateTime = currentTime;
                deltaU--;
            }

            if ( window.getWindowOptions().showFps && updateTime - lastFps > 1000000000 ) {
                lastFps = updateTime;
                window.setWindowTitle(windowTitle + " - " + fps + " FPS");
                fps = 0;
            }
            fps++;
            gameLogic.render(window, scene, renderer);
            if (!engineProperties.useVulkan()) {
            	window.swapBuffers();
            }
            
            if ( !window.isvSync() ) {
                sync();
            }
        }

        cleanup();
    }

    protected void cleanup() {
        gameLogic.cleanup(renderer);
        window.cleanup();
    }
    
    private void sync() {
        double loopSlot = 1000000000d / engineProperties.getFps();
        double endTime = initialTime + loopSlot;
        while (System.nanoTime() < endTime) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ie) {
            	System.out.println("Failed to sync");
            }
        }
    }

    protected void update(float interval) {
    	gameLogic.update(interval, window.getMouseInput());
    }

    protected void render() {
        gameLogic.render(window, scene, renderer);
        window.update();
    }
    
    protected void load(GameItem[] items) {
    	scene.setGameItems(items);
    }
    
    public void start() {
        running = true;
        run();
    }

    public void stop() {
        running = false;
    }
}
