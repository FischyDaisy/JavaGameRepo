package main.engine;

import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Results;
import main.engine.graphics.camera.Camera;
import main.engine.graphics.vulkan.VKRenderer;
import main.engine.graphics.weather.Fog;
import main.engine.scene.SceneLight;

import java.util.Iterator;

public class GameEngine {

    public static final String DEFAULT_CAMERA_NAME = "main-cam";
    private boolean running;

    private Dominion dom;
    
    private static final EngineProperties engineProperties = EngineProperties.INSTANCE;

    private final Window window;

    private final GameLogic gameLogic;
    
    private final VKRenderer renderer;
    
    private double lastFps;
    
    private int fps;
    
    private long initialTime;
    
    private String windowTitle;

    public GameEngine(String windowTitle, boolean vSync, GameLogic gameLogic) throws Throwable {
        this(windowTitle, 0, 0, vSync, gameLogic);
    }
    
    public GameEngine(String windowTitle, GameLogic gameLogic) throws Throwable {
        this(windowTitle, 0, 0, engineProperties.isvSync(), gameLogic);
    }
    
    public GameEngine(String windowTitle, int width, int height, boolean vSync, GameLogic gameLogic) throws Throwable {
    	this.windowTitle = windowTitle;
    	window = new Window(windowTitle, width, height, vSync);
    	window.init();
        this.gameLogic = gameLogic;
        dom = Dominion.create("dom");
        initDominion();
        this.renderer = new VKRenderer(window, dom);
        gameLogic.initialize(window, dom, renderer);
        lastFps = System.nanoTime();
        fps = 0;
    }

    public void initDominion() {
        dom.createEntity(Fog.NOFOG);
        dom.createEntity(true);
        dom.createEntity(new SceneLight());
        dom.createEntity(new Camera());
        dom.createEntity(new ItemLoadTimestamp());
    }

    public void run() throws Throwable {
        initialTime = System.nanoTime();
        double timeU = 1000000000d / engineProperties.getUps();
        double deltaU = 0;

        long updateTime = initialTime;
        while (running && !window.shouldClose()) {

            for (Iterator<Results.With1<Camera>> itr = dom.findEntitiesWith(Camera.class).iterator(); itr.hasNext();) {
                Camera cam = itr.next().comp();
                cam.setHasMoved(false);
            }
            window.pollEvents();
            renderer.inputNuklear(window);

            long currentTime = System.nanoTime();
            deltaU += (currentTime - initialTime) / timeU;
            initialTime = currentTime;

            if (deltaU >= 1) {
            	long diffTimeNanos = currentTime - updateTime;
                gameLogic.inputAndUpdate(window, dom, renderer);
                updateTime = currentTime;
                deltaU--;
            }

            if ( updateTime - lastFps > 1000000000 ) {
                lastFps = updateTime;
                window.setWindowTitle(windowTitle + " - " + fps + " FPS");
                fps = 0;
            }
            fps++;
            //renderer.render(window);
            
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
    
    public void start() throws Throwable {
        running = true;
        run();
    }

    public void stop() {
        running = false;
    }
}
