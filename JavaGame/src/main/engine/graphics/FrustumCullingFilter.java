package main.engine.graphics;

import java.util.List;
import java.util.Map;

import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import main.engine.graphics.opengl.GLModel;
import main.engine.graphics.opengl.Mesh;
import main.engine.items.GameItem;

public class FrustumCullingFilter {

    private final Matrix4f prjViewMatrix;

    private FrustumIntersection frustumInt;

    public FrustumCullingFilter() {
        prjViewMatrix = new Matrix4f();
        frustumInt = new FrustumIntersection();
    }

    public void updateFrustum(Matrix4f projMatrix, Matrix4f viewMatrix) {
        // Calculate projection view matrix
        prjViewMatrix.set(projMatrix);
        prjViewMatrix.mul(viewMatrix);
        // Update frustum intersection class
        frustumInt.set(prjViewMatrix);
    }

    public void filter(Map<? extends String, List<GameItem>> mapMesh, List<? extends GLModel> modelList) {
    	for (GLModel model : modelList) {
    		String modelId = model.getModelId();
        	List<GameItem> items = mapMesh.get(modelId);
        	if (items.isEmpty()) {
        		continue;
        	}
        	filter(items, model.getBoundingRadius());
    	}
    }

    public void filter(List<GameItem> gameItems, float meshBoundingRadius) {
        float boundingRadius;
        Vector3f pos;
        for (GameItem gameItem : gameItems) {
        	if (!gameItem.isDisableFrustumCulling()) {
                boundingRadius = gameItem.getLargestScale() * meshBoundingRadius;
                pos = gameItem.getPosition();
                gameItem.setInsideFrustum(insideFrustum(pos.x, pos.y, pos.z, boundingRadius));
            }
        }
    }

    public boolean insideFrustum(float x0, float y0, float z0, float boundingRadius) {
    	return frustumInt.testSphere(x0, y0, z0, boundingRadius);
    }
}