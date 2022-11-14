package main.engine.physics;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;

import crab.newton.*;
import java.lang.foreign.*;
import main.engine.graphics.ModelData;
import main.engine.items.GameItem;
import main.engine.scene.Scene;
import main.engine.utility.ResourcePaths;

public class Physics {
	
	public static final float GRAVITY_FORCE = -9.8f;
	
	public enum CollisionPrimitive { 
		BOX("box"), CAPSULE("capsule"), CHAMFERCYLINDER("chamfercylinder"), 
		CONE("cone"), CYLINDER("cylinder"), SPHERE("sphere");
		
		private String primitiveName;
		
		CollisionPrimitive(String primitiveName) {
			this.primitiveName = primitiveName;
		}
		
		public String getName() {
			return primitiveName;
		}
	};
	
	private final List<GameItem> itemList;
	private final NewtonWorld world;
	
	public Physics() {
		itemList = new ArrayList<>();
		world = NewtonWorld.create();
	}
	
	public void cleanup() {
		world.destroyAllMaterialGroupIDs();
		world.destroyAllBodies();
		world.destroy();
	}
	
	public void registerGameItem(GameItem item) {
		itemList.add(item);
	}
	
	public void removeGameItem(GameItem item) {
		itemList.remove(item);
	}
	
	public void update(float timestep) {
		world.update(timestep);
		Matrix4f matrix = new Matrix4f();
		float[] matArr;
		for (GameItem i : itemList) {
			matArr = i.getBody().getMatrix();
			matrix.set(matArr);
			i.setMatrix(matrix);
		}
	}
	
	public static void applyGravity(MemoryAddress bodyPtr, float timestep, int threadIndex) {
		NewtonBody body = NewtonBody.wrap(bodyPtr);
		float[] mass = body.getMass();
		float[] newMass = new float[] {0f, mass[0] * GRAVITY_FORCE, 0f};
		body.setForce(newMass);
	}
	
	public GameItem createPrimitiveCollision(NewtonWorld world, List<ModelData> modelDataList, String modelId, String gameitemId,
			CollisionPrimitive primitive, float[] params, float[] offsetMatrix, float mass, MemorySession session) {
		ModelData.Material newtonMaterial = new ModelData.Material(ResourcePaths.Textures.THIS_PIC_GOES_HARD);
		List<ModelData.Material> newtonMaterials = new ArrayList<ModelData.Material>();
        newtonMaterials.add(newtonMaterial);
        float[] matArr = new float[16];
        Matrix4f matrix = new Matrix4f();
        matrix.get(matArr);
		return switch (primitive) {
			case BOX -> {
				GameItem item = new GameItem(gameitemId, modelId);
				NewtonCollision collision = NewtonBox.create(world, params[0], params[1], params[2], 0, offsetMatrix, session);
				NewtonMesh mesh = NewtonMesh.createFromCollision(collision);
				NewtonBody body = NewtonDynamicBody.create(world, collision, matArr, session);
				float[] inertiaOrigin = collision.calculateInertiaMatrix();
		    	float[] origin = new float[] {inertiaOrigin[3], inertiaOrigin[4], inertiaOrigin[5]};
		    	body.setMassMatrix(mass, mass * inertiaOrigin[0], mass * inertiaOrigin[1], mass * inertiaOrigin[2]);
		    	body.setCenterOfMass(origin);
		    	body.setForceAndTorqueCallback(Physics::applyGravity, session);
		    	item.setBody(body);
		    	mesh.applyBoxMapping(0, 0, 0, matArr);
		    	ModelData model = PhysUtils.convertToModelData(mesh, modelId, newtonMaterials);
		    	modelDataList.add(model);
		    	registerGameItem(item);
		    	collision.destroy();
		    	mesh.destroy();
				yield item;
			}
			case CAPSULE -> {
				GameItem item = new GameItem(gameitemId, modelId);
				NewtonCollision collision = NewtonCapsule.create(world, params[0], params[1], params[2], 0, offsetMatrix, session);
				NewtonMesh mesh = NewtonMesh.createFromCollision(collision);
				NewtonBody body = NewtonDynamicBody.create(world, collision, matArr, session);
				float[] inertiaOrigin = collision.calculateInertiaMatrix();
		    	float[] origin = new float[] {inertiaOrigin[3], inertiaOrigin[4], inertiaOrigin[5]};
		    	body.setMassMatrix(mass, mass * inertiaOrigin[0], mass * inertiaOrigin[1], mass * inertiaOrigin[2]);
		    	body.setCenterOfMass(origin);
		    	body.setForceAndTorqueCallback(Physics::applyGravity, session);
		    	item.setBody(body);
		    	mesh.applyCylindricalMapping(0, 0, matArr);
		    	ModelData model = PhysUtils.convertToModelData(mesh, modelId, newtonMaterials);
		    	modelDataList.add(model);
		    	registerGameItem(item);
		    	collision.destroy();
		    	mesh.destroy();
				yield item;
			}
			case CHAMFERCYLINDER -> {
				GameItem item = new GameItem(gameitemId, modelId);
				NewtonCollision collision = NewtonChamferCylinder.create(world, params[0], params[1], 0, offsetMatrix, session);
				NewtonMesh mesh = NewtonMesh.createFromCollision(collision);
				NewtonBody body = NewtonDynamicBody.create(world, collision, matArr, session);
				float[] inertiaOrigin = collision.calculateInertiaMatrix();
		    	float[] origin = new float[] {inertiaOrigin[3], inertiaOrigin[4], inertiaOrigin[5]};
		    	body.setMassMatrix(mass, mass * inertiaOrigin[0], mass * inertiaOrigin[1], mass * inertiaOrigin[2]);
		    	body.setCenterOfMass(origin);
		    	body.setForceAndTorqueCallback(Physics::applyGravity, session);
		    	item.setBody(body);
		    	mesh.applyCylindricalMapping(0, 0, matArr);
		    	ModelData model = PhysUtils.convertToModelData(mesh, modelId, newtonMaterials);
		    	modelDataList.add(model);
		    	registerGameItem(item);
		    	collision.destroy();
		    	mesh.destroy();
				yield item;
			}
			case CONE -> {
				GameItem item = new GameItem(gameitemId, modelId);
				NewtonCollision collision = NewtonCone.create(world, params[0], params[1], 0, offsetMatrix, session);
				NewtonMesh mesh = NewtonMesh.createFromCollision(collision);
				NewtonBody body = NewtonDynamicBody.create(world, collision, matArr, session);
				float[] inertiaOrigin = collision.calculateInertiaMatrix();
		    	float[] origin = new float[] {inertiaOrigin[3], inertiaOrigin[4], inertiaOrigin[5]};
		    	body.setMassMatrix(mass, mass * inertiaOrigin[0], mass * inertiaOrigin[1], mass * inertiaOrigin[2]);
		    	body.setCenterOfMass(origin);
		    	body.setForceAndTorqueCallback(Physics::applyGravity, session);
		    	item.setBody(body);
		    	mesh.applyBoxMapping(0, 0, 0, matArr);
		    	ModelData model = PhysUtils.convertToModelData(mesh, modelId, newtonMaterials);
		    	modelDataList.add(model);
		    	registerGameItem(item);
		    	collision.destroy();
		    	mesh.destroy();
				yield item;
			}
			case CYLINDER -> {
				GameItem item = new GameItem(gameitemId, modelId);
				NewtonCollision collision = NewtonCylinder.create(world, params[0], params[1], params[2], 0, offsetMatrix, session);
				NewtonMesh mesh = NewtonMesh.createFromCollision(collision);
				NewtonBody body = NewtonDynamicBody.create(world, collision, matArr, session);
				float[] inertiaOrigin = collision.calculateInertiaMatrix();
		    	float[] origin = new float[] {inertiaOrigin[3], inertiaOrigin[4], inertiaOrigin[5]};
		    	body.setMassMatrix(mass, mass * inertiaOrigin[0], mass * inertiaOrigin[1], mass * inertiaOrigin[2]);
		    	body.setCenterOfMass(origin);
		    	body.setForceAndTorqueCallback(Physics::applyGravity, session);
		    	item.setBody(body);
		    	mesh.applyCylindricalMapping(0, 0, matArr);
		    	ModelData model = PhysUtils.convertToModelData(mesh, modelId, newtonMaterials);
		    	modelDataList.add(model);
		    	registerGameItem(item);
		    	collision.destroy();
		    	mesh.destroy();
				yield item;
			}
			case SPHERE -> {
				GameItem item = new GameItem(gameitemId, modelId);
				NewtonCollision collision = NewtonSphere.create(world, params[0], 0, offsetMatrix, session);
				NewtonMesh mesh = NewtonMesh.createFromCollision(collision);
				NewtonBody body = NewtonDynamicBody.create(world, collision, matArr, session);
				float[] inertiaOrigin = collision.calculateInertiaMatrix();
		    	float[] origin = new float[] {inertiaOrigin[3], inertiaOrigin[4], inertiaOrigin[5]};
		    	body.setMassMatrix(mass, mass * inertiaOrigin[0], mass * inertiaOrigin[1], mass * inertiaOrigin[2]);
		    	body.setCenterOfMass(origin);
		    	body.setForceAndTorqueCallback(Physics::applyGravity, session);
		    	item.setBody(body);
		    	mesh.applySphericalMapping(0, matArr);
		    	ModelData model = PhysUtils.convertToModelData(mesh, modelId, newtonMaterials);
		    	modelDataList.add(model);
		    	registerGameItem(item);
		    	collision.destroy();
		    	mesh.destroy();
				yield item;
			}
		};
	}
}
