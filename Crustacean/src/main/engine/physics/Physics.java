package main.engine.physics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Results;
import org.joml.Matrix4f;

import crab.newton.*;
import java.lang.foreign.*;
import main.engine.graphics.ModelData;
import main.engine.items.GameItem;
import main.engine.utility.ResourcePaths;
import org.tinylog.Logger;

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
	}
	private final NewtonWorld world;
	
	public Physics() {
		world = NewtonWorld.create();
	}
	
	public void cleanup() {
		world.destroyAllMaterialGroupIDs();
		world.destroyAllBodies();
		world.destroy();
	}

	public NewtonWorld getWorld() {
		return world;
	}
	
	public void update(float timestep, Dominion dominion) {
		world.update(timestep);/**/
		Results<Results.With2<GameItem, NewtonBody>> results = dominion.findEntitiesWith(GameItem.class, NewtonBody.class);
		for (Iterator<Results.With2<GameItem, NewtonBody>> itr = results.iterator(); itr.hasNext();) {
			Results.With2<GameItem, NewtonBody> result = itr.next();
			GameItem item = result.comp1();
			NewtonBody body = result.comp2();
			item.getModelMatrix().set(body.getMatrix());
			Logger.debug("Updated Physics Item");
		}
	}
	
	public static void applyGravity(MemoryAddress bodyPtr, float timestep, int threadIndex) {
		NewtonBody body = NewtonBody.wrap(bodyPtr);
		float[] mass = body.getMass();
		float[] newMass = new float[] {0f, mass[0] * GRAVITY_FORCE, 0f};
		body.setForce(newMass);
	}
	
	public NewtonBody createPrimitiveCollision(List<ModelData> modelDataList, String modelId,
			CollisionPrimitive primitive, float[] params, float[] offsetMatrix, float mass, MemorySession session) {
		ModelData.Material newtonMaterial = new ModelData.Material(ResourcePaths.Textures.THIS_PIC_GOES_HARD);
		List<ModelData.Material> newtonMaterials = new ArrayList<ModelData.Material>();
        newtonMaterials.add(newtonMaterial);
        float[] matArr = new float[16];
        Matrix4f matrix = new Matrix4f();
        matrix.get(matArr);
		return switch (primitive) {
			case BOX -> {
				NewtonCollision collision = world.createBox(params[0], params[1], params[2], 0, offsetMatrix);
				NewtonMesh mesh = collision.createMesh();
				NewtonBody body = world.createDynamicBody(collision, matArr);
				float[] inertiaOrigin = collision.calculateInertiaMatrix();
		    	float[] origin = new float[] {inertiaOrigin[3], inertiaOrigin[4], inertiaOrigin[5]};
		    	body.setMassMatrix(mass, mass * inertiaOrigin[0], mass * inertiaOrigin[1], mass * inertiaOrigin[2]);
		    	body.setCenterOfMass(origin);
		    	body.setForceAndTorqueCallback(Physics::applyGravity, session);
		    	mesh.applyBoxMapping(0, 0, 0, matArr);
		    	ModelData model = PhysUtils.convertToModelData(mesh, modelId, newtonMaterials);
		    	modelDataList.add(model);
		    	collision.destroy();
		    	mesh.destroy();
				yield body;
			}
			case CAPSULE -> {
				NewtonCollision collision = world.createCapsule(params[0], params[1], params[2], 0, offsetMatrix);
				NewtonMesh mesh = collision.createMesh();
				NewtonBody body = world.createDynamicBody(collision, matArr);
				float[] inertiaOrigin = collision.calculateInertiaMatrix();
		    	float[] origin = new float[] {inertiaOrigin[3], inertiaOrigin[4], inertiaOrigin[5]};
		    	body.setMassMatrix(mass, mass * inertiaOrigin[0], mass * inertiaOrigin[1], mass * inertiaOrigin[2]);
		    	body.setCenterOfMass(origin);
		    	body.setForceAndTorqueCallback(Physics::applyGravity, session);
		    	mesh.applyCylindricalMapping(0, 0, matArr);
		    	ModelData model = PhysUtils.convertToModelData(mesh, modelId, newtonMaterials);
		    	modelDataList.add(model);
		    	collision.destroy();
		    	mesh.destroy();
				yield body;
			}
			case CHAMFERCYLINDER -> {
				NewtonCollision collision = world.createChamferCylinder(params[0], params[1], 0, offsetMatrix);
				NewtonMesh mesh = collision.createMesh();
				NewtonBody body = world.createDynamicBody(collision, matArr);
				float[] inertiaOrigin = collision.calculateInertiaMatrix();
		    	float[] origin = new float[] {inertiaOrigin[3], inertiaOrigin[4], inertiaOrigin[5]};
		    	body.setMassMatrix(mass, mass * inertiaOrigin[0], mass * inertiaOrigin[1], mass * inertiaOrigin[2]);
		    	body.setCenterOfMass(origin);
		    	body.setForceAndTorqueCallback(Physics::applyGravity, session);
		    	mesh.applyCylindricalMapping(0, 0, matArr);
		    	ModelData model = PhysUtils.convertToModelData(mesh, modelId, newtonMaterials);
		    	modelDataList.add(model);
		    	collision.destroy();
		    	mesh.destroy();
				yield body;
			}
			case CONE -> {
				NewtonCollision collision = world.createCone(params[0], params[1], 0, offsetMatrix);
				NewtonMesh mesh = collision.createMesh();
				NewtonBody body = world.createDynamicBody(collision, matArr);
				float[] inertiaOrigin = collision.calculateInertiaMatrix();
		    	float[] origin = new float[] {inertiaOrigin[3], inertiaOrigin[4], inertiaOrigin[5]};
		    	body.setMassMatrix(mass, mass * inertiaOrigin[0], mass * inertiaOrigin[1], mass * inertiaOrigin[2]);
		    	body.setCenterOfMass(origin);
		    	body.setForceAndTorqueCallback(Physics::applyGravity, session);
		    	mesh.applyBoxMapping(0, 0, 0, matArr);
		    	ModelData model = PhysUtils.convertToModelData(mesh, modelId, newtonMaterials);
		    	modelDataList.add(model);
		    	collision.destroy();
		    	mesh.destroy();
				yield body;
			}
			case CYLINDER -> {
				NewtonCollision collision = world.createCylinder(params[0], params[1], params[2], 0, offsetMatrix);
				NewtonMesh mesh = collision.createMesh();
				NewtonBody body = world.createDynamicBody(collision, matArr);
				float[] inertiaOrigin = collision.calculateInertiaMatrix();
		    	float[] origin = new float[] {inertiaOrigin[3], inertiaOrigin[4], inertiaOrigin[5]};
		    	body.setMassMatrix(mass, mass * inertiaOrigin[0], mass * inertiaOrigin[1], mass * inertiaOrigin[2]);
		    	body.setCenterOfMass(origin);
		    	body.setForceAndTorqueCallback(Physics::applyGravity, session);
		    	mesh.applyCylindricalMapping(0, 0, matArr);
		    	ModelData model = PhysUtils.convertToModelData(mesh, modelId, newtonMaterials);
		    	modelDataList.add(model);
		    	collision.destroy();
		    	mesh.destroy();
				yield body;
			}
			case SPHERE -> {
				NewtonCollision collision = world.createSphere(params[0], 0, offsetMatrix);
				NewtonMesh mesh = collision.createMesh();
				NewtonBody body = world.createDynamicBody(collision, matArr);
				float[] inertiaOrigin = collision.calculateInertiaMatrix();
		    	float[] origin = new float[] {inertiaOrigin[3], inertiaOrigin[4], inertiaOrigin[5]};
		    	body.setMassMatrix(mass, mass * inertiaOrigin[0], mass * inertiaOrigin[1], mass * inertiaOrigin[2]);
		    	body.setCenterOfMass(origin);
		    	body.setForceAndTorqueCallback(Physics::applyGravity, session);
		    	mesh.applySphericalMapping(0, matArr);
		    	ModelData model = PhysUtils.convertToModelData(mesh, modelId, newtonMaterials);
		    	modelDataList.add(model);
		    	collision.destroy();
		    	mesh.destroy();
				yield body;
			}
		};
	}
}
