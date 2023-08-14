package main.engine;

import dev.dominion.ecs.api.*;
import main.engine.graphics.vulkan.VKRenderer;
import main.engine.physics.Physics;
import main.engine.scene.Scene;
import org.tinylog.Logger;

import java.util.concurrent.locks.StampedLock;

public final class Engine {

    private final Dominion dominion;
    private final Physics physics;
    private final Scheduler scheduler;
    private final Scene scene;
    private final Window window;
    private final VKRenderer renderer;
    private final StampedLock lock;
    private GameLogic game;
    private boolean running;

    public Engine(String windowTitle, int width, int height) throws Exception {
        EngineProperties props = EngineProperties.INSTANCE;
        dominion = Dominion.create("Engine-Dominion");
        scheduler = dominion.createScheduler();
        scene = new Scene(dominion);
        physics = new Physics();
        window = new Window(windowTitle, width, height, props.isvSync());
        renderer = new VKRenderer(window, dominion);
        lock = new StampedLock();
        createSchedule();
    }

    private void createSchedule() {
        //cameras
        scheduler.schedule(() -> {});
        //input
        scheduler.schedule(() -> {});
        //game & physics
        scheduler.schedule(() -> {});
        //Render
        scheduler.schedule(() -> {});
    }

    public GameLogic getGame() {
        return game;
    }

    public void setGame(GameLogic game) {
        this.game = game;
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
