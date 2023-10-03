package main.engine.scene;

import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Entity;
import main.engine.EngineProperties;
import main.engine.enginelayouts.Vector3fLayout;
import main.engine.enginelayouts.Vector4fLayout;
import main.engine.graphics.lights.Light;
import main.engine.items.GameItem;
import main.engine.items.GameItemAnimation;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.lang.foreign.*;
import java.util.Objects;

public record Scene(MemorySegment gameItems, MemorySegment lights, Arena itemArena, Arena lightArena, long itemPos, long lightPos, long gameItemsLoadedTimestamp) {
    public Scene {
        Objects.requireNonNull(gameItems);
        Objects.requireNonNull(lights);
        Objects.requireNonNull(itemArena);
        Objects.requireNonNull(lightArena);
    }

    public Scene(MemorySegment gameItems, MemorySegment lights, long itemPos, long lightPos, long gameItemsLoadedTimestamp) {
        this(gameItems, lights, Arena.openShared(), Arena.openShared(), itemPos, lightPos, gameItemsLoadedTimestamp);
    }
}
