package main.engine.physics;

import jdk.incubator.foreign.*;

public class NewtonLoader {
	
	public static final String filePath = "\\src\\main\\resources\\clib\\Bullet3.dll";
	
	
	
	public NewtonLoader() {
	}
	
	public static SymbolLookup loadLib() {
		System.load(System.getProperty("user.dir") + filePath);
		return SymbolLookup.loaderLookup();
	}
}
