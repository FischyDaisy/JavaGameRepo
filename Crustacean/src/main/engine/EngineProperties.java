package main.engine;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

import main.engine.utility.ResourcePaths;
import org.tinylog.Logger;

public class EngineProperties {
	private static final float DEFAULT_FOV = 60.0f;
	private static final int DEFAULT_JOINT_MATRICES_BUF = 2000000;
    private static final int DEFAULT_MAX_ANIM_WEIGHTS_BUF = 100000;

    private static final int DEFAULT_MAX_ANIM_VERTICES_BUF = 20000000;
    private static final int DEFAULT_MAX_INDICES_BUF = 5000000;
    private static final int DEFAULT_MAX_MATERIALS = 500;
    private static final int DEFAULT_MAX_VERTICES_BUF = 20000000;
	private static final int DEFAULT_MAX_SKYBOX_MATERIALS = 10;
	private static final int DEFAULT_MAX_SKYBOX_VERTICES_BUF = 2000;
	private static final int DEFAULT_MAX_SKYBOX_INDICES_BUF = 1000;
    private static final int DEFAULT_MAX_PHYSICS_VERTICES_BUF = 200000;
    private static final int DEFAULT_MAX_PHYSICS_INDICES_BUF = 100000;
    private static final long DEFAULT_MAX_GAMEITEM_BUF = 2000000;
    private static final long DEFAULT_MAX_LIGHT_BUF = 2000;
	private static final int DEFAULT_REQUESTED_IMAGES = 3;
	private static final float DEFAULT_SHADOW_BIAS = 0.00005f;
    private static final int DEFAULT_SHADOW_MAP_SIZE = 2048;
    private static final int DEFAULT_UPS = 30;
    private static final int DEFAULT_FPS = 60;
    private static final float DEFAULT_Z_FAR = 1000.f;
    private static final float DEFAULT_Z_NEAR = 0.01f;
    private static final File PROP_FILE = new File(ResourcePaths.Engine.PROPERTIES);
    private String defaultTexturePath;
    private float fov;
    private int maxAnimWeightsBuffer;

    private int maxAnimVerticesBuffer;
    private int maxIndicesBuffer;
    private int maxJointMatricesBuffer;
    private int maxMaterials;
    private int maxTextures;
    private int maxVerticesBuffer;
    private int maxSkyboxMaterials;
    private int maxSkyboxTextures;
    private int maxSkyboxVerticesBuffer;
    private int maxSkyboxIndicesBuffer;
    private int maxPhysicsVerticesBuffer;
    private int maxPhysicsIndicesBuffer;
    private long maxGameItemBuffer;
    private long maxLightBuffer;
    private String physDeviceName;
    private int requestedImages;
    private boolean shaderRecompilation;
    private float shadowBias;
    private boolean shadowDebug;
    private int shadowMapSize;
    private boolean shadowPcf;
    private int ups;
    private int fps;
    private boolean useVulkan;
    private boolean vSync;
    private boolean validate;
    private float zFar;
    private float zNear;
    
    public static final EngineProperties INSTANCE = new EngineProperties();

    private EngineProperties() {
        // Singleton
        Properties props = new Properties();
        
        //EngineProperties.class.getResourceAsStream("/main/resources/" + FILENAME)

        try (InputStream stream = Files.newInputStream(PROP_FILE.toPath(), StandardOpenOption.READ)) {
            props.load(stream);
            ups = Integer.parseInt(props.getOrDefault("ups", DEFAULT_UPS).toString());
            fps = Integer.parseInt(props.getOrDefault("fps", DEFAULT_FPS).toString());
            useVulkan = Boolean.parseBoolean(props.getOrDefault("useVulkan", false).toString());
            validate = Boolean.parseBoolean(props.getOrDefault("vkValidate", false).toString());
            physDeviceName = props.getProperty("physDeviceName");
            requestedImages = Integer.parseInt(props.getOrDefault("requestedImages", DEFAULT_REQUESTED_IMAGES).toString());
            vSync = Boolean.parseBoolean(props.getOrDefault("vsync", true).toString());
            shaderRecompilation = Boolean.parseBoolean(props.getOrDefault("shaderRecompilation", false).toString());
            fov = (float) Math.toRadians(Float.parseFloat(props.getOrDefault("fov", DEFAULT_FOV).toString()));
            zNear = Float.parseFloat(props.getOrDefault("zNear", DEFAULT_Z_NEAR).toString());
            zFar = Float.parseFloat(props.getOrDefault("zFar", DEFAULT_Z_FAR).toString());
            defaultTexturePath = props.getProperty("defaultTexturePath");
            maxMaterials = Integer.parseInt(props.getOrDefault("maxMaterials", DEFAULT_MAX_MATERIALS).toString());
            maxSkyboxMaterials = Integer.parseInt(props.getOrDefault("maxSkyboxMaterials", DEFAULT_MAX_SKYBOX_MATERIALS).toString());
            maxSkyboxVerticesBuffer = Integer.parseInt(props.getOrDefault("maxSkyboxVerticesBuffer", DEFAULT_MAX_SKYBOX_VERTICES_BUF).toString());
            maxSkyboxIndicesBuffer = Integer.parseInt(props.getOrDefault("maxSkyboxIndicesBuffer", DEFAULT_MAX_SKYBOX_INDICES_BUF).toString());
            shadowPcf = Boolean.parseBoolean(props.getOrDefault("shadowPcf", false).toString());
            shadowBias = Float.parseFloat(props.getOrDefault("shadowBias", DEFAULT_SHADOW_BIAS).toString());
            shadowMapSize = Integer.parseInt(props.getOrDefault("shadowMapSize", DEFAULT_SHADOW_MAP_SIZE).toString());
            shadowDebug = Boolean.parseBoolean(props.getOrDefault("shadowDebug", false).toString());
            maxTextures = maxMaterials * 3;
            maxSkyboxTextures = maxSkyboxMaterials * 3;
            maxVerticesBuffer = Integer.parseInt(props.getOrDefault("maxVerticesBuffer", DEFAULT_MAX_VERTICES_BUF).toString());
            maxIndicesBuffer = Integer.parseInt(props.getOrDefault("maxIndicesBuffer", DEFAULT_MAX_INDICES_BUF).toString());
            maxAnimWeightsBuffer = Integer.parseInt(props.getOrDefault("maxAnimWeightsBuffer", DEFAULT_MAX_ANIM_WEIGHTS_BUF).toString());
            maxAnimVerticesBuffer = Integer.parseInt(props.getOrDefault("maxAnimVerticesBuffer", DEFAULT_MAX_ANIM_VERTICES_BUF).toString());
            maxJointMatricesBuffer = Integer.parseInt(props.getOrDefault("maxJointMatricesBuffer", DEFAULT_JOINT_MATRICES_BUF).toString());
            maxPhysicsVerticesBuffer = Integer.parseInt(props.getOrDefault("maxPhysicsVerticesBuffer", DEFAULT_MAX_PHYSICS_VERTICES_BUF).toString());
            maxPhysicsIndicesBuffer = Integer.parseInt(props.getOrDefault("maxPhysicsIndicesBuffer", DEFAULT_MAX_PHYSICS_INDICES_BUF).toString());
            maxGameItemBuffer = Long.parseLong(props.getOrDefault("maxGameItemsBuffer", DEFAULT_MAX_GAMEITEM_BUF).toString());
            maxLightBuffer = Long.parseLong(props.getOrDefault("maxLightBuffer", DEFAULT_MAX_LIGHT_BUF).toString());
        } catch (IOException excp) {
            Logger.error("Could not read [{}] properties file", PROP_FILE.toPath());
            //System.out.println(excp);
        }
    }
    
    public String getDefaultTexturePath() {
        return defaultTexturePath;
    }
    
    public String getPhysDeviceName() {
        return physDeviceName;
    }
    
    public int getRequestedImages() {
        return requestedImages;
    }
    
    public float getShadowBias() {
        return shadowBias;
    }

    public int getShadowMapSize() {
        return shadowMapSize;
    }
    
    public int getUps() {
        return ups;
    }
    
    public int getFps() {
        return fps;
    }
    
    public float getFOV() {
    	return fov;
    }
    
    public float getZNear() {
    	return zNear;
    }
    
    public float getZFar() {
    	return zFar;
    }
    
    public int getMaxAnimWeightsBuffer() {
        return maxAnimWeightsBuffer;
    }

    public int getMaxAnimVerticesBuffer() {
        return maxAnimVerticesBuffer;
    }

    public int getMaxIndicesBuffer() {
        return maxIndicesBuffer;
    }

    public int getMaxJointMatricesBuffer() {
        return maxJointMatricesBuffer;
    }

    public int getMaxMaterials() {
        return maxMaterials;
    }

    public int getMaxTextures() {
        return maxTextures;
    }
    
    public int getMaxVerticesBuffer() {
        return maxVerticesBuffer;
    }
    
    public int getMaxSkyboxMaterials() {
    	return maxSkyboxMaterials;
    }
    
    public int getMaxSkyboxTextures() {
    	return maxSkyboxTextures;
    }
    
    public int getMaxSkyboxVerticesBuffer() {
    	return maxSkyboxVerticesBuffer;
    }
    
    public int getMaxSkyboxIndicesBuffer() {
    	return maxSkyboxIndicesBuffer;
    }

    public int getMaxPhysicsIndicesBuffer() {
        return maxPhysicsIndicesBuffer;
    }

    public int getMaxPhysicsVerticesBuffer() {
        return maxPhysicsVerticesBuffer;
    }

    public long getMaxGameItemBuffer() {
        return maxGameItemBuffer;
    }

    public long getMaxLightBuffer() {
        return maxLightBuffer;
    }
    
    public boolean isShaderRecompilation() {
        return shaderRecompilation;
    }
    
    public boolean isShadowDebug() {
        return shadowDebug;
    }

    public boolean isShadowPcf() {
        return shadowPcf;
    }
    
    public boolean useVulkan() {
    	return useVulkan;
    }

    public boolean isValidate() {
        return validate;
    }
    
    public boolean isvSync() {
        return vSync;
    }
}