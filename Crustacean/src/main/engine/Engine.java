package main.engine;

import dev.dominion.ecs.api.*;
import main.engine.graphics.camera.Camera;
import main.engine.graphics.vulkan.VKRenderer;
import main.engine.input.EngineInput;
import main.engine.physics.Physics;
import main.engine.scene.Scene;
import main.engine.scripts.SceneAssetCloser;
import main.engine.scripts.SceneAssetLoader;
import main.engine.scripts.EntityUpdater;
import main.engine.sound.SoundManager;
import org.tinylog.Logger;

import java.lang.foreign.Arena;
import java.util.concurrent.locks.StampedLock;

public final class Engine {

    private final Dominion dominion;
    private final Physics physics;
    private final Scheduler scheduler;
    private final Window window;
    private final VKRenderer renderer;
    private final SoundManager soundManager;
    private final EngineInput engineInput;
    private final StampedLock lock;
    private GameLogic game;
    private Arena itemArena, lightArena;
    private boolean running;
    private double deltaU;

    public Engine(String windowTitle, int width, int height) throws Exception {
        EngineProperties props = EngineProperties.INSTANCE;
        dominion = Dominion.create("Engine-Dominion");
        scheduler = dominion.createScheduler();
        physics = new Physics();
        window = new Window(windowTitle, width, height, props.isvSync());
        engineInput = new EngineInput(window.getWindowHandle());
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
            for (var result : dominion.findEntitiesWith(Camera.class)) {
                Camera cam = result.comp();
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
                    for (var result : dominion.findEntitiesWith(EntityUpdater.class).withAlso(Loaded.class)) {
                        result.comp().lambda().execute(result.entity(), engineInput);
                    }
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

    public Dominion getDominion() {
        return dominion;
    }

    public void loadGame(GameLogic game) {
        this.game = game;
        try {
            game.initialize(window, dominion, renderer, physics, soundManager);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void loadAllResources() {
        var scenes = dominion.findEntitiesWith(Scene.class);


        for (var scene : scenes) {
            var results = scene.comp().findSceneEntities().withAlso(SceneAssetLoader.class)
                    .without(Loaded.class);
            for (var result : results) {
                result.entity().get(SceneAssetLoader.class).lambda().execute(scene.comp(), renderer, physics);
                result.entity().add(new Loaded());
            }
        }
        try {
            renderer.loadModels();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void loadSceneResources(Scene scene) {
        var results = dominion.findEntitiesWith(SceneAssetLoader.class)
                .withAlso(scene.getTag().getClass())
                .without(Loaded.class);

        for (var result : results) {
            result.comp().lambda().execute(scene, renderer, physics);
            result.entity().add(new Loaded());
        }
        try {
            renderer.loadModels();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void unloadAllResources() {
        var results = dominion.findEntitiesWith(SceneAssetCloser.class).withAlso(Loaded.class);

        for (var result : results) {
            result.comp().lambda().execute(dominion, renderer, physics);
            result.entity().removeType(Loaded.class);
        }
        renderer.unloadModels();
    }

    public void unloadSceneResources(Scene scene) {
        var results = dominion.findEntitiesWith(SceneAssetCloser.class)
                .withAlso(scene.getTag().getClass(), Loaded.class);

        for (var result : results) {
            result.comp().lambda().execute(dominion, renderer, physics);
            result.entity().removeType(Loaded.class);
        }
        renderer.unloadModels();
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
        renderer.cleanup();
        physics.cleanup();
        dominion.close();
        window.cleanup();
    }
}
