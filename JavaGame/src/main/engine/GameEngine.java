package main.engine;

import main.engine.graphics.Renderer;
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
    
    private final Renderer renderer;
    
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
        this.renderer = opts.useVulkan == false ? new GLRenderer(window) : new VKRenderer(window, scene);
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

        	scene.getCamera().setHasMoved(false);
            window.pollEvents();

            long currentTime = System.nanoTime();
            deltaU += (currentTime - initialTime) / timeU;
            initialTime = currentTime;

            gameLogic.input(window, scene, currentTime - updateTime);
            if (deltaU >= 1) {
                gameLogic.update(timeU, window);
                updateTime = currentTime;
                deltaU--;
            }

            if ( window.getOptions().showFps && updateTime - lastFps > 1000000000 ) {
                lastFps = updateTime;
                window.setWindowTitle(windowTitle + " - " + fps + " FPS");
                fps = 0;
            }
            fps++;
            gameLogic.render(window, scene);
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
        gameLogic.cleanup();
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
    	gameLogic.update(interval, window);
    }

    protected void render() {
        gameLogic.render(window, scene);
        window.update();
    }
    
    public void start() {
        running = true;
        run();
    }

    public void stop() {
        running = false;
    }
}
