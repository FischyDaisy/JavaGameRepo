package main.engine.physics.collision.shapes;


/**
 * ConeShape implements a cone shape, around the Z axis.
 *
 * @author jezek2
 */
public class ConeShapeZ extends ConeShape {

	public ConeShapeZ(float radius, float height) {
		super(radius, height);
		setConeUpIndex(2);
	}

}
