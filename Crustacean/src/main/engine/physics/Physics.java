package main.engine.physics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Results;
import main.engine.enginelayouts.Matrix4fLayout;
import main.engine.enginelayouts.Vector3fLayout;
import main.engine.enginelayouts.Vector4fLayout;
import org.joml.Matrix4f;

import crab.newton.*;
import java.lang.foreign.*;
import main.engine.graphics.ModelData;
import main.engine.items.GameItem;
import main.engine.utility.ResourcePaths;
import org.tinylog.Logger;

import static crab.newton.Newton.*;

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
		Newton.loadNewton(ResourcePaths.Newton.NEWTON_DLL);
		world = NewtonWorld.create();
	}
	
	public void cleanup() {
		world.destroyAllMaterialGroupIDs();
		world.destroyAllBodies();
		world.destroy();
		Newton.unloadNewton();
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
	
	public static void applyGravity(MemorySegment bodyPtr, float timestep, int threadIndex) {
		Logger.error("Callback Body Scope: {}", bodyPtr.scope());
		NewtonBody body = new NewtonBody(bodyPtr);
		try (Arena arena = Arena.openConfined()) {
			MemorySegment curMass = arena.allocate(VEC4F);
			body.getMass(
					curMass.asSlice(0L),
					curMass.asSlice(C_FLOAT.byteSize()),
					curMass.asSlice(C_FLOAT.byteSize() * 2),
					curMass.asSlice(C_FLOAT.byteSize() * 3)
			);
			float force = Vector4fLayout.getX(curMass) * GRAVITY_FORCE;
			MemorySegment forceSegment = arena.allocate(VEC3F);
			Vector3fLayout.setY(forceSegment, force);
			body.setForce(forceSegment);
		}
	}
	
	public NewtonBody createPrimitiveCollision(List<ModelData> modelDataList, String modelId,
			CollisionPrimitive primitive, float[] params, float[] offsetMatrix, float mass, Arena arena) throws Throwable {
		ModelData.Material newtonMaterial = new ModelData.Material(ResourcePaths.Textures.THIS_PIC_GOES_HARD);
		List<ModelData.Material> newtonMaterials = new ArrayList<ModelData.Material>();
        newtonMaterials.add(newtonMaterial);
        MemorySegment matArr = Matrix4fLayout.identity(arena);
		MemorySegment offMat = arena.allocateArray(C_FLOAT, offsetMatrix);
		return switch (primitive) {
			case BOX -> {
				try (Arena pArena = Arena.openConfined()) {
					NewtonCollision collision = world.createBox(params[0], params[1], params[2], 0, offMat);
					NewtonMesh mesh = collision.createMesh();
					NewtonBody body = world.createDynamicBody(collision, matArr);
					Logger.error("Box Body Scope: {}", body.address().scope());
					MemorySegment inertiaOrigin = pArena.allocateArray(VEC3F, 2);
					collision.calculateInertiaMatrix(inertiaOrigin);
					body.setMassMatrix(mass,
							mass * inertiaOrigin.getAtIndex(C_FLOAT, 0),
							mass * inertiaOrigin.getAtIndex(C_FLOAT, 1),
							mass * inertiaOrigin.getAtIndex(C_FLOAT, 2));
					body.setCenterOfMass(inertiaOrigin.asSlice(VEC3F.byteSize()));
					body.setForceAndTorqueCallback(Physics::applyGravity, SegmentScope.global());
					mesh.applyBoxMapping(0, 0, 0, matArr);
					ModelData model = PhysUtils.convertToModelData(mesh, modelId, newtonMaterials);
					modelDataList.add(model);
					collision.destroy();
					mesh.destroy();
					yield body;
				}
			}
			case CAPSULE -> {
				NewtonCollision collision = world.createCapsule(params[0], params[1], params[2], 0, offsetMatrix);
				NewtonMesh mesh = collision.createMesh();
				NewtonBody body = world.createDynamicBody(collision, matArr);
				Logger.error("Capsule Body Scope: {}", body.address().scope());
				float[] inertiaOrigin = collision.calculateInertiaMatrix();
		    	float[] origin = new float[] {inertiaOrigin[3], inertiaOrigin[4], inertiaOrigin[5]};
		    	body.setMassMatrix(mass, mass * inertiaOrigin[0], mass * inertiaOrigin[1], mass * inertiaOrigin[2]);
		    	body.setCenterOfMass(origin);
		    	body.setForceAndTorqueCallback(Physics::applyGravity, SegmentScope.global());
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
				Logger.error("ChamferCylinder Body Scope: {}", body.address().scope());
				float[] inertiaOrigin = collision.calculateInertiaMatrix();
		    	float[] origin = new float[] {inertiaOrigin[3], inertiaOrigin[4], inertiaOrigin[5]};
		    	body.setMassMatrix(mass, mass * inertiaOrigin[0], mass * inertiaOrigin[1], mass * inertiaOrigin[2]);
		    	body.setCenterOfMass(origin);
		    	body.setForceAndTorqueCallback(Physics::applyGravity, SegmentScope.global());
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
				Logger.error("Cone Body Scope: {}", body.address().scope());
				float[] inertiaOrigin = collision.calculateInertiaMatrix();
		    	float[] origin = new float[] {inertiaOrigin[3], inertiaOrigin[4], inertiaOrigin[5]};
		    	body.setMassMatrix(mass, mass * inertiaOrigin[0], mass * inertiaOrigin[1], mass * inertiaOrigin[2]);
		    	body.setCenterOfMass(origin);
		    	body.setForceAndTorqueCallback(Physics::applyGravity, SegmentScope.global());
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
				Logger.error("Cylinder Body Scope: {}", body.address().scope());
				float[] inertiaOrigin = collision.calculateInertiaMatrix();
		    	float[] origin = new float[] {inertiaOrigin[3], inertiaOrigin[4], inertiaOrigin[5]};
		    	body.setMassMatrix(mass, mass * inertiaOrigin[0], mass * inertiaOrigin[1], mass * inertiaOrigin[2]);
		    	body.setCenterOfMass(origin);
		    	body.setForceAndTorqueCallback(Physics::applyGravity, SegmentScope.global());
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
				Logger.error("Sphere Body Scope: {}", body.address().scope());
				float[] inertiaOrigin = collision.calculateInertiaMatrix();
		    	float[] origin = new float[] {inertiaOrigin[3], inertiaOrigin[4], inertiaOrigin[5]};
		    	body.setMassMatrix(mass, mass * inertiaOrigin[0], mass * inertiaOrigin[1], mass * inertiaOrigin[2]);
		    	body.setCenterOfMass(origin);
		    	body.setForceAndTorqueCallback(Physics::applyGravity, SegmentScope.global());
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
