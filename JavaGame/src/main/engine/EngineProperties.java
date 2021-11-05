package main.engine;

import java.io.*;
import java.util.Properties;

public class EngineProperties {
	private static final float DEFAULT_FOV = 60.0f;
	private static final int DEFAULT_REQUESTED_IMAGES = 3;
    private static final int DEFAULT_UPS = 30;
    private static final int DEFAULT_FPS = 60;
    private static final float DEFAULT_Z_FAR = 1000.f;
    private static final float DEFAULT_Z_NEAR = 0.01f;
    private static final String FILENAME = "eng.properties";
    private static EngineProperties instance;
    private String defaultTexturePath;
    private float fov;
    private String physDeviceName;
    private int requestedImages;
    private boolean shaderRecompilation;
    private int ups;
    private int fps;
    private boolean useVulkan;
    private boolean vSync;
    private boolean validate;
    private float zFar;
    private float zNear;

    private EngineProperties() {
        // Singleton
        Properties props = new Properties();

        try (InputStream stream = EngineProperties.class.getResourceAsStream("/main/resources/" + FILENAME)) {
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
    
    public boolean isShaderRecompilation() {
        return shaderRecompilation;
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