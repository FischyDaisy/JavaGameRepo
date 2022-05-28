package main.game;

import com.newton.*;
import com.newton.generated.*;

import main.engine.GameEngine;
import main.engine.IGameLogic;
import main.engine.Window;
public class Main {
 
    public static void main(String[] args) {
    	RuntimeHelper.loadLibraryAbsolute("C:\\Users\\Christopher\\Documents\\Workspace\\JavaGame\\resources\\newtondll\\newton.dll");
    	
        System.out.println("NewtonWorld Version: " + NewtonWorld.getWorldVersion());
        
        NewtonWorld world = NewtonWorld.create();
        
        System.out.println("Max Threads: " + world.getMaxThreadCount());
    }
}