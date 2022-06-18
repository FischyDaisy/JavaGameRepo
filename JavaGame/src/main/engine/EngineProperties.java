package main.engine;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

import main.engine.utility.ResourcePaths;

public class EngineProperties {
	private static final float DEFAULT_FOV = 60.0f;
	private static final int DEFAULT_MAX_JOINTS_MATRICES_LISTS = 100;
    private static final int DEFAULT_STORAGES_BUFFERS = 100;
	private static final int DEFAULT_MAX_MATERIALS = 500;
	private static final int DEFAULT_REQUESTED_IMAGES = 3;
	private static final float DEFAULT_SHADOW_BIAS = 0.00005f;
    private static final int DEFAULT_SHADOW_MAP_SIZE = 2048;
    private static final int DEFAULT_UPS = 30;
    private static final int DEFAULT_FPS = 60;
    private static final float DEFAULT_Z_FAR = 1000.f;
    private static final float DEFAULT_Z_NEAR = 0.01f;
    private static final String FILENAME = "eng.properties";
    private static final File PROP_FILE = new File(ResourcePaths.Engine.PROPERTIES);
    private static EngineProperties instance;
    private String defaultTexturePath;
    private float fov;
    private int maxJointsMatricesLists;
    private int maxStorageBuffers;
    private int maxMaterials;
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
    private boolean useDeferred;

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
            useDeferred = Boolean.parseBoolean(props.getOrDefault("useDeferred", false).toString());
            maxMaterials = Integer.parseInt(props.getOrDefault("maxMaterials", DEFAULT_MAX_MATERIALS).toString());
            shadowPcf = Boolean.parseBoolean(props.getOrDefault("shadowPcf", false).toString());
            shadowBias = Float.parseFloat(props.getOrDefault("shadowBias", DEFAULT_SHADOW_BIAS).toString());
            shadowMapSize = Integer.parseInt(props.getOrDefault("shadowMapSize", DEFAULT_SHADOW_MAP_SIZE).toString());
            shadowDebug = Boolean.parseBoolean(props.getOrDefault("shadowDebug", false).toString());
            maxStorageBuffers = Integer.parseInt(props.getOrDefault("maxStorageBuffers", DEFAULT_STORAGES_BUFFERS).toString());
            maxJointsMatricesLists = Integer.parseInt(props.getOrDefault("maxJointsMatricesLists", DEFAULT_MAX_JOINTS_MATRICES_LISTS).toString());
        } catch (IOException excp) {
        	System.out.println("Could not read [" + FILENAME + "] properties file");
            //System.out.println(excp);
        }
    }

    public static synchronized EngineProperties getInstance() {
        if (instance == null) {
            instance = new EngineProperties();
        }
        return instance;
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
    
    public int getMaxJointsMatricesLists() {
        return maxJointsMatricesLists;
    }

    public int getMaxStorageBuffers() {
        return maxStorageBuffers;
    }
    
    public int getMaxMaterials() {
        return maxMaterials;
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
    
    public boolean useDeferred() {
    	return useDeferred;
    }
}