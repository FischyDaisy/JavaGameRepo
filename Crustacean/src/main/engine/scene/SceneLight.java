package main.engine.scene;

import java.util.Arrays;
import java.util.Optional;

import org.joml.Vector3f;
import org.joml.Vector4f;

import main.engine.graphics.GraphConstants;
import main.engine.graphics.lights.DirectionalLight;
import main.engine.graphics.lights.Light;
import main.engine.graphics.lights.PointLight;
import main.engine.graphics.lights.SpotLight;

public class SceneLight {

    private final Vector4f ambientLight;
    
    private final Vector3f skyBoxLight;
    
    private PointLight[] pointLightList;
    
    private SpotLight[] spotLightList;
    
    private Light directionalLight;
    
    private boolean lightChanged;
    
    private Light[] lights;
    
    public SceneLight() {
    	ambientLight = new Vector4f();
    	skyBoxLight = new Vector3f();
    }

    public Vector4f getAmbientLight() {
        return ambientLight;
    }
    
    public Light[] getLights() {
        return this.lights;
    }
    
    public boolean isLightChanged() {
        return lightChanged;
    }
    
    public void setLightChanged(boolean lightChanged) {
        this.lightChanged = lightChanged;
    }
    
    public void setLights(Light[] lights) {
    	directionalLight = null;
        int numLights = lights != null ? lights.length : 0;
        if (numLights > GraphConstants.MAX_LIGHTS) {
            throw new RuntimeException("Maximum number of lights set to: " + GraphConstants.MAX_LIGHTS);
        }
        this.lights = lights;
        Optional<Light> option = Arrays.stream(lights).filter(l -> l.getPosition().w == 0).findFirst();
        if (option.isPresent()) {
            directionalLight = option.get();
        }

        lightChanged = true;
    }

    public PointLight[] getPointLightList() {
        return pointLightList;
    }

    public void setPointLightList(PointLight[] pointLightList) {
        this.pointLightList = pointLightList;
    }

    public SpotLight[] getSpotLightList() {
        return spotLightList;
    }

    public void setSpotLightList(SpotLight[] spotLightList) {
        this.spotLightList = spotLightList;
    }

    public Light getDirectionalLight() {
        return directionalLight;
    }

    public void setDirectionalLight(Light directionalLight) {
        this.directionalLight = directionalLight;
    }
    
    public Vector3f getSkyBoxLight() {
        return skyBoxLight;
    }
}