package main.engine.graphics.particles;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.joml.Vector3f;

import main.engine.items.GameItem;

public class FlowParticleEmitter implements IParticleEmitter {

    private int maxParticles;

    private boolean active;

    private final List<GameItem> particles;

    private final Particle baseParticle;

    private long creationPeriodNanos;

    private long lastCreationTime;

    private float speedAndRange;

    private float positionAndRange;

    private float scaleAndRange;
    
    private long animRange;

    public FlowParticleEmitter(Particle baseParticle, int maxParticles, long creationPeriodNanos) {
        particles = new ArrayList<GameItem>();
        this.baseParticle = baseParticle;
        this.maxParticles = maxParticles;
        this.active = false;
        this.lastCreationTime = 0;
        this.creationPeriodNanos = creationPeriodNanos;
    }

    @Override
    public Particle getBaseParticle() {
        return baseParticle;
    }

    public long getCreationPeriodNanos() {
        return creationPeriodNanos;
    }

    public int getMaxParticles() {
        return maxParticles;
    }

    @Override
    public List<GameItem> getParticles() {
        return particles;
    }

    public float getPositionAndRange() {
        return positionAndRange;
    }

    public float getScaleAndRange() {
        return scaleAndRange;
    }

    public float getSpeedAndRange() {
        return speedAndRange;
    }
    
    public void setAnimRange(long animRange) {
        this.animRange = animRange;
    }

    public void setCreationPeriodNanos(long creationPeriodNanos) {
        this.creationPeriodNanos = creationPeriodNanos;
    }

    public void setMaxParticles(int maxParticles) {
        this.maxParticles = maxParticles;
    }

    public void setPositionAndRange(float positionAndRange) {
        this.positionAndRange = positionAndRange;
    }

    public void setScaleAndRange(float scaleAndRange) {
        this.scaleAndRange = scaleAndRange;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setSpeedAndRange(float speedAndRange) {
        this.speedAndRange = speedAndRange;
    }

    public void update(long elapsedTime) {
        long now = System.nanoTime();
        if (lastCreationTime == 0) {
            lastCreationTime = now;
        }
        Iterator<? extends GameItem> it = particles.iterator();
        while (it.hasNext()) {
            Particle particle = (Particle) it.next();
            if (particle.updateTtl(elapsedTime) < 0) {
                it.remove();
            } else {
                updatePosition(particle, elapsedTime);
            }
        }

        int length = this.getParticles().size();
        if (now - lastCreationTime >= this.creationPeriodNanos && length < maxParticles) {
            createParticle();
            this.lastCreationTime = now;
        }
    }

    private void createParticle() {
        Particle particle = new Particle(this.getBaseParticle());
        // Add a little bit of randomness of the parrticle
        float sign = Math.random() > 0.5d ? -1.0f : 1.0f;
        float speedInc = sign * (float)Math.random() * this.speedAndRange;
        float posInc = sign * (float)Math.random() * this.positionAndRange;
        float scaleInc = sign * (float)Math.random() * this.scaleAndRange;
        long updateAnimInc = (long)sign *(long)(Math.random() * (float)this.animRange);
        particle.getPosition().add(posInc, posInc, posInc);
        particle.getSpeed().add(speedInc, speedInc, speedInc);
        particle.setScale(particle.getScale().add(scaleInc, scaleInc, scaleInc));
        particle.setUpdateTextureNanos(particle.getUpdateTextureNanos() + updateAnimInc);
        particles.add(particle);
    }

    /**
     * Updates a particle position
     * @param particle The particle to update
     * @param elapsedTime Elapsed time in nanoseconds
     */
    public void updatePosition(Particle particle, long elapsedTime) {
        Vector3f speed = particle.getSpeed();
        float delta = elapsedTime;
        float dx = speed.x * (delta / 1000000000);
        float dy = speed.y * (delta / 1000000000);
        float dz = speed.z * (delta / 1000000000);
        Vector3f pos = particle.getPosition();
        particle.setPosition(pos.x + dx, pos.y + dy, pos.z + dz);
    }

    @Override
    public void cleanup() {
        for (GameItem particle : getParticles()) {
            particle.cleanup();
        }
    }
}