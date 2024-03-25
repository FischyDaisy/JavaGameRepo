package main.engine.scene;

import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Entity;
import dev.dominion.ecs.api.Results;
import main.engine.graphics.lights.AmbientLight;
import main.engine.graphics.lights.Light;
import main.engine.items.GameItem;
import main.engine.items.GameItemAnimation;

public final class Scene {

    public final Dominion dominion;
    public final String title;
    public final Tag tag;
    public AmbientLight ambientLight;

    public Scene(Dominion dominion, String title, Tag tag, AmbientLight ambientLight) {
        this.dominion = dominion;
        this.title = title;
        this.tag = tag;
        this.ambientLight = ambientLight;
    }

    public Scene(Dominion dominion, String title, Tag tag) {
        this(dominion, title, tag, new AmbientLight());
    }

    public Dominion getDominion() {
        return dominion;
    }

    public Tag getTag() {
        return tag;
    }
    public String getTitle() {
        return title;
    }

    public AmbientLight getAmbientLight() {
        return ambientLight;
    }

    public void setAmbientLight(AmbientLight ambientLight) {
        this.ambientLight = ambientLight;
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
        GameItem item = new GameItem(modelId);
        return addStaticGameItem(item);
    }

    public Entity addStaticGameItem(GameItem item) {
        return dominion.createEntity(tag, item);
    }

    public Entity[] addStaticGameItems(String modelId, int count) {
        Entity[] entities = new Entity[count];
        for (int i = 0; i < count; i++) {
            entities[i] = addStaticGameItem(modelId);
        }
        return entities;
    }

    public Entity addLight() {
        Light light = new Light();
        return addLight(light);
    }

    public Entity addLight(Light light) {
        return dominion.createEntity(tag, light);
    }

    public Entity[] addLights(int count) {
        Entity[] entities = new Entity[count];
        for (int i = 0; i < count; i++) {
            entities[i] = addLight();
        }
        return entities;
    }

    public Results<? extends Results.With1<? extends Tag>> findSceneEntities() {
        return dominion.findEntitiesWith(tag.getClass());
    }

    public void removeAllEntities() {
        var results = findSceneEntities();
        for (var result: results) {
            dominion.deleteEntity(result.entity());
        }
    }

    public abstract static class Tag {
        public Tag() {}
    }
}
