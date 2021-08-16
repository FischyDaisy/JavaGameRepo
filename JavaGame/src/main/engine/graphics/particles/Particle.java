package main.engine.graphics.particles;

import org.joml.Vector3f;

import main.engine.graphics.opengl.Mesh;
import main.engine.graphics.opengl.Texture;
import main.engine.items.GameItem;

public class Particle extends GameItem {
	
	private long updateTextureNanos;
    
    private long currentAnimTimeNanos;

    private Vector3f speed;

    /**
     * Time to live for particle in nanoseconds.
     */
    private long ttl;
    
    private int animFrames;

    public Particle(Mesh mesh, Vector3f speed, long ttl, long updateTextureNanos) {
        super(mesh);
        this.speed = new Vector3f(speed);
        this.ttl = ttl;
        this.updateTextureNanos = updateTextureNanos;
        this.currentAnimTimeNanos = 0;
        Texture texture = this.getMesh().getMaterial().getTexture();
        this.animFrames = texture.getNumCols() * texture.getNumRows();
    }

    public Particle(Particle baseParticle) {
        super(baseParticle.getMesh());
        Vector3f aux = baseParticle.getPosition();
        setPosition(aux.x, aux.y, aux.z);
        aux = baseParticle.getRotation();
        setRotation(aux.x, aux.y, aux.z);
        setScale(baseParticle.getScale());
        this.speed = new Vector3f(baseParticle.speed);
        this.ttl = baseParticle.geTtl();
        this.updateTextureNanos = baseParticle.getUpdateTextureNanos();
        this.currentAnimTimeNanos = 0;
        this.animFrames = baseParticle.getAnimFrames();
    }
    
    public int getAnimFrames() {
        return animFrames;
    }

    public Vector3f getSpeed() {
        return speed;
    }
    
    public long getUpdateTextureNanos() {
        return updateTextureNanos;
    }
    
    public void setUpdateTextureNanos(long updateTextureNanos) {
        this.updateTextureNanos = updateTextureNanos;
    }

    public void setSpeed(Vector3f speed) {
        this.speed = speed;
    }

    public long geTtl() {
        return ttl;
    }

    public void setTtl(long ttl) {
        this.ttl = ttl;
    }
    
    /**
     * Updates the Particle's TTL
     * @param elapsedTime Elapsed Time in nanoseconds
     * @return The Particle's TTL
     */
    public long updateTtl(long elapsedTime) {
    	this.ttl -= elapsedTime;
        this.currentAnimTimeNanos += elapsedTime;
        if ( this.currentAnimTimeNanos >= this.getUpdateTextureNanos() && this.animFrames > 0 ) {
            this.currentAnimTimeNanos = 0;
            int pos = this.getTextPos();
            pos++;
            if ( pos < this.animFrames ) {
                this.setTextPos(pos);
            } else {
                this.setTextPos(0);
            }
        }
        return this.ttl;
    }
    
}