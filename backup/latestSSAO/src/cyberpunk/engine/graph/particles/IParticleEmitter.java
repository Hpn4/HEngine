package hengine.engine.graph.particles;

import java.util.List;

import hengine.engine.world.item.GameItem;

public interface IParticleEmitter {

    void cleanup();
    
    Particle getBaseParticle();
    
    List<GameItem> getParticles();
}
