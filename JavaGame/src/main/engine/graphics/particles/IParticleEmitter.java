package main.engine.graphics.particles;

import java.util.List;
import main.engine.items.GameItem;

public interface IParticleEmitter {

    void cleanup();
    
    Particle getBaseParticle();
    
    List<GameItem> getParticles();
}
