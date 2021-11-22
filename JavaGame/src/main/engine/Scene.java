package main.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.engine.graphics.opengl.InstancedMesh;
import main.engine.graphics.opengl.Mesh;
import main.engine.graphics.particles.IParticleEmitter;
import main.engine.graphics.weather.Fog;
import main.engine.items.GameItem;
import main.engine.items.Portal;
import main.engine.items.SkyBox;

public class Scene {

	private final Map<Mesh, List<GameItem>> meshMap;
	
	private final Map<String, List<GameItem>> modelMap;
	
	private final Map<String, List<GameItem>> instancedModelMap;
	
	private final Map<InstancedMesh, List<GameItem>> instancedMeshMap;
	
	private final Map<Mesh, List<GameItem>> portalMap;
    
    private SkyBox skyBox;
    
    private SceneLight sceneLight;
    
    private Fog fog;
    
    private boolean renderShadows;
    
    private IParticleEmitter[] particleEmitters;
    
    public Scene() {
        meshMap = new HashMap<Mesh, List<GameItem>>();
        modelMap = new HashMap<String, List<GameItem>>();
        instancedModelMap = new HashMap<String, List<GameItem>>();
        instancedMeshMap = new HashMap<InstancedMesh, List<GameItem>>();
        portalMap = new HashMap<Mesh, List<GameItem>>();
        fog = Fog.NOFOG;
        renderShadows = true;
    }

    public Map<Mesh, List<GameItem>> getGameMeshes() {
        return meshMap;
    }
    
    public Map<InstancedMesh, List<GameItem>> getGameInstancedMeshes() {
        return instancedMeshMap;
    }
    
    public Map<Mesh, List<GameItem>> getPortalMeshes() {
        return portalMap;
    }
    
    public Map<String, List<GameItem>> getModelMap() {
    	return modelMap;
    }
    
    public boolean isRenderShadows() {
        return renderShadows;
    }
    
    public boolean containsPortals() {
    	for (Map.Entry<? extends Mesh, List<GameItem>> entry : portalMap.entrySet()) {
    		List<GameItem> gameItems = entry.getValue();
    		for (GameItem item : gameItems) {
    			if (item.isInsideFrustum()) {
    				return true;
    			}
    		}
    	}
    	return false;
    }
    
    public void addGameItem(GameItem gameItem) {
    	List<GameItem> gameItems = modelMap.get(gameItem.getModelId());
        if (gameItems == null) {
            gameItems = new ArrayList<>();
            modelMap.put(gameItem.getModelId(), gameItems);
        }
        gameItems.add(gameItem);
    }
    
    public List<GameItem> getGameItemsByModelId(String modelId) {
        return modelMap.get(modelId);
    }

    public void setGameItems(GameItem[] gameItems) {
    	// Create a map of meshes to speed up rendering
        int numGameItems = gameItems != null ? gameItems.length : 0;
        for (int i = 0; i < numGameItems; i++) {
            GameItem gameItem = gameItems[i];
            boolean isPortal = gameItem instanceof Portal;
            Mesh[] meshes = gameItem.getMeshes();
            for (Mesh mesh : meshes) {
                boolean instancedMesh = mesh instanceof InstancedMesh;
                List<GameItem> list = instancedMesh ? instancedMeshMap.get(mesh) : meshMap.get(mesh);
                list = isPortal ? portalMap.get(mesh) : list;
                if (list == null) {
                    list = new ArrayList<>();
                    if (instancedMesh) {
                        instancedMeshMap.put((InstancedMesh)mesh, list);
                    } else {
                    	if (isPortal) {
                    		portalMap.put(mesh, list);
                    	} else {
                    		meshMap.put(mesh, list);
                    	}
                    }
                }
                list.add(gameItem);
            }
        }
    }
    
    public void cleanup() {
    	for (Mesh mesh : meshMap.keySet()) {
            mesh.cleanup();
        }
        for (Mesh mesh : instancedMeshMap.keySet()) {
            mesh.cleanup();
        }
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