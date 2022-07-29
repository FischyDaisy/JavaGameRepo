package main.engine.scene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.engine.graphics.camera.Camera;
import main.engine.graphics.particles.IParticleEmitter;
import main.engine.graphics.weather.Fog;
import main.engine.items.GameItem;
import main.engine.items.SkyBox;

public class Scene {
	
	private final Map<String, List<GameItem>> modelMap;
	
	private final Map<String, List<GameItem>> instancedModelMap;
	
	private Camera camera;
    
    private SkyBox skyBox;
    
    private SceneLight sceneLight;
    
    private Fog fog;
    
    private boolean renderShadows;
    
    private IParticleEmitter[] particleEmitters;
    
    public Scene() {
        modelMap = new HashMap<String, List<GameItem>>();
        instancedModelMap = new HashMap<String, List<GameItem>>();
        fog = Fog.NOFOG;
        renderShadows = true;
        sceneLight = new SceneLight();
    }
    
    public Map<String, List<GameItem>> getModelMap() {
    	return modelMap;
    }
    
    public Map<String, List<GameItem>> getInstancedModelMap() {
    	return instancedModelMap;
    }
    
    public boolean isRenderShadows() {
        return renderShadows;
    }
    
    public boolean containsPortals() {
    	/*
    	for (Map.Entry<? extends Mesh, List<GameItem>> entry : portalMap.entrySet()) {
    		List<GameItem> gameItems = entry.getValue();
    		for (GameItem item : gameItems) {
    			if (item.isInsideFrustum()) {
    				return true;
    			}
    		}
    	}
    	*/
    	return false;
    }
    
    public void addGameItem(GameItem gameItem) {
    	List<GameItem> gameItems = modelMap.get(gameItem.getModelId());
        if (gameItems == null) {
            gameItems = new ArrayList<GameItem>();
            modelMap.put(gameItem.getModelId(), gameItems);
        }
        gameItems.add(gameItem);
    }
    
    public void addInstancedGameItem(GameItem gameItem) {
    	List<GameItem> gameItems = instancedModelMap.get(gameItem.getModelId());
        if (gameItems == null) {
            gameItems = new ArrayList<GameItem>();
            instancedModelMap.put(gameItem.getModelId(), gameItems);
        }
        gameItems.add(gameItem);
    }
    
    public List<GameItem> getGameItemsByModelId(String modelId) {
        return modelMap.get(modelId);
    }
    
    public List<GameItem> getInstancedGameItemsByModelId(String modelId) {
        return instancedModelMap.get(modelId);
    }
    
    public void clearGameItemsByModelId(String modelId) {
    	modelMap.get(modelId).clear();
    }
    
    public void clearInstancedGameItemsByModelId(String modelId) {
    	instancedModelMap.get(modelId).clear();
    }
    
    public void cleanup() {
        if (particleEmitters != null) {
            for (IParticleEmitter particleEmitter : particleEmitters) {
                particleEmitter.cleanup();
            }
        }
    }

    public SkyBox getSkyBox() {
        return skyBox;
    }

    public void setSkyBox(SkyBox skyBox) {
        this.skyBox = skyBox;
    }
    
    public void setRenderShadows(boolean renderShadows) {
        this.renderShadows = renderShadows;
    }

    public SceneLight getSceneLight() {
        return sceneLight;
    }

    public void setSceneLight(SceneLight sceneLight) {
        this.sceneLight = sceneLight;
    }
    
    public Camera getCamera() {
    	return camera;
    }
    
    public void setCamera(Camera camera) {
    	this.camera = camera;
    }
    
    /**
     * @return the fog
     */
    public Fog getFog() {
        return fog;
    }

    /**
     * @param fog the fog to set
     */
    public void setFog(Fog fog) {
        this.fog = fog;
    }
    
    public IParticleEmitter[] getParticleEmitters() {
        return particleEmitters;
    }

    public void setParticleEmitters(IParticleEmitter[] particleEmitters) {
        this.particleEmitters = particleEmitters;
    }
}