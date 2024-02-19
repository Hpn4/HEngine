package hengine.engine.graph.particles;

import java.util.ArrayList;
import java.util.List;

public abstract class IParticleEmitter {

	protected final List<Particle> particles;

	protected final Particle baseParticle;

	public IParticleEmitter(final Particle baseParticle) {
		particles = new ArrayList<>();

		this.baseParticle = baseParticle;
	}

	public void cleanup() {
		for (final Particle particle : getParticles())
			particle.cleanup();
	}

	public Particle getBaseParticle() {
		return baseParticle;
	}

	public List<Particle> getParticles() {
		return particles;
	}
}
