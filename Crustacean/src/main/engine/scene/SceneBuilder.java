package main.engine.scene;

import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Entity;
import main.engine.Engine;
import main.engine.EngineProperties;
import main.engine.graphics.lights.Light;
import main.engine.graphics.lights.SingletonLights;
import main.engine.items.GameItem;
import main.engine.items.GameItemAnimation;

import java.lang.foreign.*;

public class SceneBuilder {

    private static final long CHUNK_SIZE = 2000; //In bytes
    private final Dominion dominion;
    private Scene scene;
    private SingletonLights singletonLights;

    public SceneBuilder(Engine engine, Dominion dominion, Scene scene) {
        this.dominion = dominion;
        this.scene = scene;
        singletonLights = new SingletonLights(this.scene.lights());
        dominion.createEntity(singletonLights);
    }

    public SceneBuilder(Engine engine, Dominion dominion) {
        this.dominion = dominion;
        EngineProperties props = EngineProperties.INSTANCE;
        Arena itemArena = Arena.ofShared();
        Arena lightArena = Arena.ofShared();
        MemorySegment items = itemArena.allocateArray(GameItem.LAYOUT, props.getMaxGameItemBuffer());
        long totalBufferSize = SingletonLights.LAYOUT.byteSize() +
                (Light.LAYOUT.byteSize() * props.getMaxLightBuffer());
        MemorySegment lights = lightArena.allocate(totalBufferSize);
        this.scene = new Scene(items, lights, itemArena, lightArena, 0l, SingletonLights.LAYOUT.byteSize(), System.currentTimeMillis());
        singletonLights = new SingletonLights(lights);
        dominion.createEntity(singletonLights);
    }

    public Dominion getDominion() {
        return dominion;
    }

    private boolean checkBuffer(MemorySegment buffer, long bufferOffset, MemoryLayout layout) {
        return (buffer.byteSize() - bufferOffset) < layout.byteSize();
    }

    public Entity addAnimatedGameItem(String modelId, int maxFrames) {
        Entity entity = addStaticGameItem(modelId);
        GameItemAnimation animation = new GameItemAnimation(false, 0, 0, maxFrames);
        entity.add(animation);
        return entity;
    }

    public Entity addAnimatedGameItem(String modelId, GameItemAnimation animation) {
        Entity entity = addStaticGameItem(modelId);
        entity.add(animation);
        return entity;
    }

    public Entity[] addAnimatedGameItems(String modelId, int maxFrames, int count) {
        Entity[] entities = addStaticGameItems(modelId, count);
        for (Entity entity : entities) {
            GameItemAnimation animation = new GameItemAnimation(false, 0, 0, maxFrames);
            entity.add(animation);
        }
        return entities;
    }

    public Entity addStaticGameItem(String modelId) {
        MemorySegment itemBuffer = scene.gameItems();
        Arena itemArena = scene.itemArena();
        long pos = scene.itemPos();
        if (checkBuffer(itemBuffer, pos, GameItem.LAYOUT)) {
            long currentBufferSize = scene.gameItems().byteSize();
            Arena tempArena = Arena.ofShared();
            MemorySegment tempBuffer = tempArena.allocate(currentBufferSize + CHUNK_SIZE);
            MemorySegment.copy(itemBuffer, 0, tempBuffer, 0, pos);
            itemBuffer = tempBuffer;
            itemArena.close();
            itemArena = tempArena;
        }

        long layoutSize = GameItem.LAYOUT.byteSize();
        GameItem item = new GameItem(modelId,
                itemBuffer.asSlice(pos, layoutSize));
        Entity entity = dominion.createEntity(item);
        pos += layoutSize;
        scene = new Scene(itemBuffer, scene.lights(),
                itemArena, scene.lightArena(),
                pos, scene.lightPos(), System.currentTimeMillis());
        return entity;
    }

    public Entity[] addStaticGameItems(String modelId, int count) {
        Entity[] entities = new Entity[count];
        for (int i = 0; i < count; i++) {
            entities[i] = addStaticGameItem(modelId);
        }
        return entities;
    }

    public Entity addLight() {
        MemorySegment lightBuffer = scene.lights();
        Arena lightArena = scene.lightArena();
        long pos = scene.lightPos();
        if (checkBuffer(lightBuffer, pos, Light.LAYOUT)) {
            long currentBufferSize = scene.gameItems().byteSize();
            Arena tempArena = Arena.ofShared();
            MemorySegment tempBuffer = lightArena.allocate(currentBufferSize + CHUNK_SIZE);
            MemorySegment.copy(lightBuffer, 0, tempBuffer, 0, pos);
            lightBuffer = tempBuffer;
            lightArena.close();
            lightArena = tempArena;
            singletonLights = new SingletonLights(lightBuffer);
        }

        long layoutSize = Light.LAYOUT.byteSize();
        Light light = new Light(lightBuffer.asSlice(pos, layoutSize));
        Entity entity = dominion.createEntity(light);
        pos += layoutSize;
        scene = new Scene(scene.gameItems(), lightBuffer,
                scene.itemArena(), lightArena,
                scene.itemPos(), pos, System.currentTimeMillis());
        return entity;
    }

    public Entity[] addLights(int count) {
        Entity[] entities = new Entity[count];
        for (int i = 0; i < count; i++) {
            entities[i] = addLight();
        }
        return entities;
    }
}
