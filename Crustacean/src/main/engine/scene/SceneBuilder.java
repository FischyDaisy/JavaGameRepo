package main.engine.scene;

import main.engine.EngineProperties;
import main.engine.graphics.lights.Light;
import main.engine.items.GameItem;

import java.lang.foreign.*;

public class SceneBuilder {

    private static final long CHUNK_SIZE = 2000; //In bytes
    private final Dominion dominion;
    private Scene scene;

    public SceneBuilder(Dominion dominion, Scene scene) {
        this.dominion = dominion;
        this.scene = scene;
    }

    public SceneBuilder(Dominion dominion) {
        this.dominion = dominion;
        EngineProperties props = EngineProperties.INSTANCE;
        Arena itemArena = Arena.ofShared();
        Arena lightArena = Arena.ofShared();
        MemorySegment items = itemArena.allocateArray(GameItem.LAYOUT, props.getMaxGameItemBuffer());
        long totalBufferSize = Scene.SINGLETON_LIGHTS.byteSize() +
                (Light.LAYOUT.byteSize() * props.getMaxLightBuffer());
        MemorySegment lights = lightArena.allocate(totalBufferSize);
        this.scene = new Scene(items, lights, itemArena, lightArena, 0l, 0l, System.currentTimeMillis());
    }

    public Dominion getDominion() {
        return dominion;
    }

    private boolean checkBuffer(MemorySegment buffer, long bufferOffset, MemoryLayout layout) {
        return (buffer.byteSize() - bufferOffset) < layout.byteSize();
    }

    public Entity addStaticGameItem(String modelId) {
        MemorySegment itemBuffer = scene.gameItems();
        Arena itemArena = scene.itemArena();
        long pos = scene.itemPos();
        if (checkBuffer(scene.gameItems(), scene.itemPos(), GameItem.LAYOUT)) {
            long currentBufferSize = scene.gameItems().byteSize();
            itemArena.close();
            itemArena = Arena.ofShared();
            itemBuffer = itemArena.allocate(currentBufferSize + CHUNK_SIZE);
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
}
