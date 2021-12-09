package main.engine.graphics.particles;

import java.util.List;

import main.engine.graphics.ModelData;
import main.engine.graphics.opengl.InstancedGLModel;
import main.engine.items.GameItem;

public interface IParticleEmitter {

    public void cleanup();
    
    public Particle getBaseParticle();
    
    public List<GameItem> getParticles();
}
