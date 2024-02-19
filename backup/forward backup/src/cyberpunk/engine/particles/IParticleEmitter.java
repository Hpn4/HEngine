package hengine.engine.particles;

import java.util.List;

import hengine.engine.item.GameItem;

public interface IParticleEmitter {

    void cleanup();
    
    Particle getBaseParticle();
    
    List<GameItem> getParticles();
}
