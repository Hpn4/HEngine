package hengine.engine.graph.particles;

import org.joml.Vector3f;

import hengine.engine.graph.mesh.AbstractMesh;
import hengine.engine.world.item.GameItem;

public class Particle extends GameItem {

	private Vector3f speed;

	/**
	 * Time to live for particle in milliseconds.
	 */
	private long ttl;

	private final long baseTtl;

	public Particle(final AbstractMesh mesh, final Vector3f speed, final long ttl) {
		super(mesh);

		this.speed = new Vector3f(speed);
		this.ttl = ttl;
		baseTtl = ttl;
	}

	public Particle(final Particle baseParticle) {
		super(baseParticle);
		speed = new Vector3f(baseParticle.speed);

		ttl = baseParticle.ttl;
		baseTtl = baseParticle.baseTtl;
	}

	public Vector3f getSpeed() {
		return speed;
	}

	public void setSpeed(final Vector3f speed) {
		this.speed = speed;
	}

	public long geTtl() {
		return ttl;
	}

	public void setTtl(final long ttl) {
		this.ttl = ttl;
	}

	/**
	 * Updates the Particle's TTL
	 * 
	 * @param elapsedTime Elapsed Time in milliseconds
	 * @return The Particle's TTL
	 */
	public long updateTtl(final long elapsedTime) {
		ttl -= elapsedTime;

		return ttl;
	}

	public float getTtlRatio() {
		return ((float) baseTtl - ttl) / (float) baseTtl;
	}
}