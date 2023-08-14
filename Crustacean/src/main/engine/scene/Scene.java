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

public class Scene {

    private final Dominion dominion;
    private SceneItems gameItems;
    private SceneLights lights;
    private Arena itemArena, lightArena;
    private long gameItemLoadedTimestamp;


    public Scene(Dominion dominion) {
        EngineProperties props = EngineProperties.INSTANCE;
        this.dominion = dominion;
        itemArena = Arena.openShared();
        MemorySegment buffer = itemArena.allocateArray(GameItem.LAYOUT, SceneItems.GAMEITEM_BUFFER_SIZE);
        gameItems = new SceneItems(buffer, 0);
        lightArena = Arena.openShared();
        long totalSize = SceneLights.SINGLETON_LIGHTS.byteSize() + (Light.LAYOUT.byteSize() * SceneLights.LIGHT_BUFFER_SIZE);
        buffer = lightArena.allocate(totalSize);
        lights = new SceneLights(buffer, SceneLights.SINGLETON_LIGHTS.byteSize());
        gameItemLoadedTimestamp = System.currentTimeMillis();
    }

    public Scene(String name) {
        this(Dominion.create(name));
    }

    public Scene() {
        this(Dominion.create());
    }

    public Dominion getDominion() {
        return dominion;
    }

    /**
     * Creates a {@code GameItem} with the specified modelId
     * @param modelId {@code String} representing the modelId
     * @return GameItem with specified modelId and a unique slice of
     * the managers internal {@code MemorySegment}
     */
    public Entity createAnimatedGameItem(String modelId, int maxFrames) {
        Entity entity = createStaticGameItem(modelId);
        GameItemAnimation animation = new GameItemAnimation(false, 0, 0, maxFrames);
        entity.add(animation);
        return entity;
    }

    public Entity createAnimatedGameItem(String modelId, GameItemAnimation animation) {
        Entity entity = createStaticGameItem(modelId);
        entity.add(animation);
        return entity;
    }

    public Entity[] createAnimatedGameItems(String modelId, int maxFrames, int count) {
        Entity[] entities = createStaticGameItems(modelId, count);
        for (Entity entity : entities) {
            GameItemAnimation animation = new GameItemAnimation(false, 0, 0, maxFrames);
            entity.add(animation);
        }
        return entities;
    }

    public Entity createStaticGameItem(String modelId) {
        MemorySegment itemBuffer = gameItems.gameItems();
        long pos = gameItems.pos();

        GameItem item = new GameItem(modelId,
                itemBuffer.asSlice(pos, SceneItems.GAMEITEM_LAYOUT_SIZE));
        Entity entity = dominion.createEntity(item);
        pos += SceneItems.GAMEITEM_LAYOUT_SIZE;
        gameItems = new SceneItems(itemBuffer, pos);
        gameItemLoadedTimestamp = System.currentTimeMillis();
        return entity;
    }

    public Entity[] createStaticGameItems(String modelId, int count) {
        Entity[] entities = new Entity[count];
        MemorySegment itemBuffer = gameItems.gameItems();
        long pos = gameItems.pos();

        for (int i = 0; i < count; i++) {
            GameItem item = new GameItem(modelId,
                    itemBuffer.asSlice(pos, SceneItems.GAMEITEM_LAYOUT_SIZE));
            Entity entity = dominion.createEntity(item);
            entities[i] = entity;
            pos += SceneItems.GAMEITEM_LAYOUT_SIZE;
        }
        gameItems = new SceneItems(itemBuffer, pos);
        gameItemLoadedTimestamp = System.currentTimeMillis();
        return entities;
    }

    public Entity createLight() {
        MemorySegment lightBuffer = lights.lights();
        long pos = lights.pos();

        Light light = new Light(lightBuffer.asSlice(pos, SceneLights.LIGHT_LAYOUT_SIZE));
        Entity entity = dominion.createEntity(light);
        pos += SceneLights.LIGHT_LAYOUT_SIZE;
        lights = new SceneLights(lightBuffer, pos);
        return entity;
    }

    public Entity[] createLights(int count) {
        Entity[] entities = new Entity[count];
        MemorySegment lightBuffer = lights.lights();
        long pos = lights.pos();

        for (int i = 0; i < count; i++) {
            Light light = new Light(lightBuffer.asSlice(pos, SceneLights.LIGHT_LAYOUT_SIZE));
            Entity entity = dominion.createEntity(light);
            entities[i] = entity;
            pos += SceneLights.LIGHT_LAYOUT_SIZE;
        }
        lights = new SceneLights(lightBuffer, pos);
        return entities;
    }

    public Vector4f getAmbientLight() {
        return Vector4fLayout.getVector4f(lights.lights());
    }

    public void setAmbientLight(Vector4f value) {
        Vector4fLayout.setVector4f(lights.lights(), value);
    }

    public Vector3f getSkyboxLight() {
        long offset = Vector4fLayout.LAYOUT.byteSize();
        return Vector3fLayout.getVector3f(lights.lights(), offset);
    }

    public void setSkyboxLight(Vector3f value) {
        long offset = Vector4fLayout.LAYOUT.byteSize();
        Vector3fLayout.setVector3f(lights.lights(), offset, value);
    }

    public long getGameItemLoadedTimestamp() {
        return gameItemLoadedTimestamp;
    }

    public void releaseGameItems() {
        itemArena.close();
        itemArena = Arena.openShared();
        MemorySegment buffer = itemArena.allocateArray(GameItem.LAYOUT, SceneItems.GAMEITEM_BUFFER_SIZE);
        gameItems = new SceneItems(buffer, 0);
    }

    public void releaseLights() {
        lightArena.close();
        lightArena = Arena.openShared();
        long totalSize = SceneLights.SINGLETON_LIGHTS.byteSize() + (Light.LAYOUT.byteSize() * SceneLights.LIGHT_BUFFER_SIZE);
        MemorySegment buffer = lightArena.allocate(totalSize);
        lights = new SceneLights(buffer, SceneLights.SINGLETON_LIGHTS.byteSize());
    }

    public void cleanup() {
        releaseLights();
        releaseGameItems();
    }

    private record SceneItems(MemorySegment gameItems, long pos) {
        public static final long GAMEITEM_BUFFER_SIZE = EngineProperties.INSTANCE.getMaxGameItemBuffer();
        public static final long GAMEITEM_LAYOUT_SIZE = GameItem.LAYOUT.byteSize();
    }

    private record SceneLights(MemorySegment lights, long pos) {
        public static final long LIGHT_BUFFER_SIZE = EngineProperties.INSTANCE.getMaxLightBuffer();
        public static final long LIGHT_LAYOUT_SIZE = Light.LAYOUT.byteSize();
        public static final MemoryLayout SINGLETON_LIGHTS = MemoryLayout.structLayout(
                Vector4fLayout.LAYOUT.withName("ambientLight"),
                Vector3fLayout.LAYOUT.withName("skyBoxLight")
        );
    }
}
