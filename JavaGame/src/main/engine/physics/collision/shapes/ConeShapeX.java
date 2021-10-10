package main.engine.physics.collision.shapes;


/**
 * ConeShape implements a cone shape, around the X axis.
 * 
 * @author jezek2
 */
public class ConeShapeX extends ConeShape {

	public ConeShapeX(float radius, float height) {
		super(radius, height);
		setConeUpIndex(0);
	}

}