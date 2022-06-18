package main.game;

import java.util.Arrays;

import org.joml.Matrix4f;

import com.newton.*;
import com.newton.generated.*;
import jdk.incubator.foreign.*;

public class Main {
 
    public static void main(String[] args) {
    	RuntimeHelper.loadLibraryAbsolute("C:\\Users\\Christopher\\Documents\\Workspace\\JavaGame\\resources\\newtondll\\newton.dll");
    	
        System.out.println("NewtonWorld Version: " + NewtonWorld.getWorldVersion());
        
        NewtonWorld world = NewtonWorld.create();
        
        System.out.println("Max Threads: " + world.getMaxThreadCount());
        
        try (ResourceScope scope = ResourceScope.newConfinedScope()) {
        	SegmentAllocator allocator = SegmentAllocator.nativeAllocator(scope);
        	
        	NewtonCollision boxCollision = NewtonBox.create(world, 10f, 10f, 10f, 0, null, allocator);
        	
        	Matrix4f matrix = new Matrix4f();
        	float[] matArr = new float[16];
        	matrix.transpose();
        	matrix.get(matArr);
        	
        	NewtonBody dBody = NewtonDynamicBody.create(world, boxCollision, matArr, allocator);
        	dBody.setMassMatrix(10f, 0f, 0f, 0f);
        	
        	System.out.println("Position: " + Arrays.toString(dBody.getPosition(allocator)));
        	
        	dBody.setForceAndTorqueCallback((bodyPtr, timestep, threadIndex) -> {
        		try (ResourceScope ftScope = ResourceScope.newConfinedScope()) {
        			SegmentAllocator ftAlloc = SegmentAllocator.nativeAllocator(ftScope);
        			
        			NewtonBody body = NewtonBody.wrap(bodyPtr);
            		float[] mass = body.getMass(ftAlloc);
            		System.out.println("Mass: " + Arrays.toString(mass));
            		float[] newMass = new float[] {0f, mass[0] * -9.8f, 0f};
            		body.setForce(newMass, ftAlloc);
        		}
        	}, scope);
        	
        	world.update(10f);
        	
        	System.out.println("Position: " + Arrays.toString(dBody.getPosition(allocator)));
        }
    }
}