package main.engine;

import java.io.*;
import java.util.Properties;

public class EngineProperties {
	private static final int DEFAULT_REQUESTED_IMAGES = 3;
    private static final int DEFAULT_UPS = 30;
    private static final int DEFAULT_FPS = 60;
    private static final String FILENAME = "eng.properties";
    private static EngineProperties instance;
    private String physDeviceName;
    private int requestedImages;
    private boolean shaderRecompilation;
    private int ups;
    private int fps;
    private boolean useVulkan;
    private boolean vSync;
    private boolean validate;

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