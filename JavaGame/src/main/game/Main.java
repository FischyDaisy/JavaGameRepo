package main.game;

import java.util.Arrays;

import org.joml.Matrix4f;

import crab.newton.*;
import jdk.incubator.foreign.*;

public class Main {
 
    public static void main(String[] args) {
    	Newton.loadNewtonAbsolute("C:\\Users\\Christopher\\Documents\\Workspace\\JavaGame\\resources\\newtondll\\newton.dll");
    	
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
        	
        	System.out.println("Position: " + Arrays.toString(dBody.getPosition()));
        	
        	dBody.setForceAndTorqueCallback((bodyPtr, timestep, threadIndex) -> {
        		NewtonBody body = NewtonBody.wrap(bodyPtr);
        		float[] mass = body.getMass();
        		System.out.println("Mass: " + Arrays.toString(mass));
        		float[] newMass = new float[] {0f, mass[0] * -9.8f, 0f};
        		body.setForce(newMass);
        	}, scope);
        	
        	world.update(10f);
        	
        	System.out.println("Position: " + Arrays.toString(dBody.getPosition()));
        	
        	NewtonMesh mesh = NewtonMesh.createFromCollision(boxCollision);
        	
        	int vertexCount = mesh.getPointCount();
        	
        	System.out.println("Box Vertex Count: " + vertexCount);
        	
        	float[] vertexData = mesh.getVertexChannel(vertexCount);
        	System.out.println("Box Vertex List: " + Arrays.toString(vertexData));
        	float[] normalData, biNormalData, uvData;
        	if (mesh.hasNormalChannel()) {
        		normalData = mesh.getNormalChannel(vertexCount);
        		System.out.println("Box Normal List: " + Arrays.toString(normalData));
        	}
        	if (mesh.hasBiNormalChannel()) {
        		biNormalData = mesh.getBiNormalChannel(vertexCount);
        		System.out.println("Box BiNormal List: " + Arrays.toString(biNormalData));
        	}
        	if (mesh.hasUV0Channel()) {
        		uvData = mesh.getUV0Channel(vertexCount);
        		System.out.println("Box UV List: " + Arrays.toString(uvData));
        	}
        }
    }
}