package main.engine;

import dev.dominion.ecs.api.*;
import main.engine.graphics.camera.Camera;
import main.engine.graphics.vulkan.VKRenderer;
import main.engine.physics.Physics;
import main.engine.scene.Scene;
import main.engine.sound.SoundManager;
import org.tinylog.Logger;

import java.lang.foreign.Arena;
import java.util.Iterator;
import java.util.concurrent.locks.StampedLock;

public final class Engine {

    private final Dominion dominion;
    private final Physics physics;
    private final Scheduler scheduler;
    private final Scene scene;
    private final Window window;
    private final VKRenderer renderer;
    private final SoundManager soundManager;
    private final StampedLock lock;
    private GameLogic game;
    private Arena itemArena, lightArena;
    private boolean running;
    private double deltaU;

    public Engine(String windowTitle, int width, int height) throws Exception {
        EngineProperties props = EngineProperties.INSTANCE;
        dominion = Dominion.create("Engine-Dominion");
        scheduler = dominion.createScheduler();
        scene = new Scene(dominion);
        physics = new Physics();
        window = new Window(windowTitle, width, height, props.isvSync());
        renderer = new VKRenderer(window, dominion);
        soundManager = new SoundManager();
        lock = new StampedLock();
        deltaU = 0;
        scheduleEngine();
    }

    public Engine(String windowTitle) throws Exception {
        this(windowTitle, 0, 0);
    }

    private void scheduleEngine() {
        //cameras & input
        scheduler.parallelSchedule(() -> {
            for (Iterator<Results.With1<Camera>> itr = dominion.findEntitiesWith(Camera.class).iterator(); itr.hasNext();) {
                Camera cam = itr.next().comp();
                cam.setHasMoved(false);
            }
        }, window::pollEvents);
        //game & physics
        scheduler.schedule(() -> {
            EngineProperties props = EngineProperties.INSTANCE;
            double deltaTime = scheduler.deltaTime();
            deltaU += deltaTime / props.getUps();
            if (deltaU >= 1.0) {
                try {
                    game.inputAndUpdate(window, dominion, renderer, physics, soundManager);
                    physics.update((float) deltaTime, dominion);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
        });
        //Render
        scheduler.schedule(() -> {
            try {
                renderer.render(window);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
    }

    public GameLogic getGame() {
        return game;
    }

    public void loadGame(GameLogic game) {
        this.game = game;
        try {
            game.initialize(window, scene, renderer, physics, soundManager);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isRunning() {
        long stamp = lock.tryOptimisticRead();
        boolean r = running;
        if (!lock.validate(stamp)) {
            stamp = lock.readLock();
            try {
                r = running;
            } finally {
                lock.unlockRead(stamp);
            }
        }
        return r;
    }

    public void setRunning(boolean running) {
        long stamp = lock.writeLock();
        try {
            this.running = running;
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    public void run() {
        EngineProperties props = EngineProperties.INSTANCE;
        setRunning(true);
        while (isRunning() && window.isOpen()) {
            if (props.isvSync()) {
                scheduler.tick();
            } else {
                scheduler.tickAtFixedRate(props.getFps());
                try {
                    this.wait();
                } catch (InterruptedException ignored) {}
            }
        }
        cleanup();
    }

    private void cleanup() {
        boolean shutdown = scheduler.shutDown();
        Logger.debug("Successfully Shutdown Scheduler?: {}", shutdown);
        game.cleanup();
        scene.cleanup();
        renderer.cleanup();
        physics.cleanup();
        dominion.close();
        window.cleanup();
    }
}
