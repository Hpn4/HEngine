package hengine.engine.graph.particles;

import org.joml.Vector3f;

import hengine.engine.graph.mesh.AbstractMesh;
import hengine.engine.utils.loader.texture.Texture;
import hengine.engine.world.item.GameItem;

public class Particle extends GameItem {

	private long updateTextureMillis;

	private long currentAnimTimeMillis;

	private Vector3f speed;

	/**
	 * Time to live for particle in milliseconds.
	 */
	private long ttl;

	private int animFrames;

	public Particle(final AbstractMesh mesh, final Vector3f speed, final long ttl, final long updateTextureMillis) {
		super(mesh);

		this.speed = new Vector3f(speed);
		this.ttl = ttl;
		this.updateTextureMillis = updateTextureMillis;
		currentAnimTimeMillis = 0;

		final Texture texture = (Texture) getMesh().getMaterial().getDiffuseMap();
		animFrames = texture.getNumCols() * texture.getNumRows();
	}

	public Particle(final Particle baseParticle) {
		super(baseParticle);
		speed = new Vector3f(baseParticle.speed);

		ttl = baseParticle.geTtl();
		updateTextureMillis = baseParticle.getUpdateTextureMillis();
		currentAnimTimeMillis = 0;
		animFrames = baseParticle.getAnimFrames();
	}

	public int getAnimFrames() {
		return animFrames;
	}

	public long getUpdateTextureMillis() {
		return updateTextureMillis;
	}

	public void setUpdateTextureMills(final long updateTextureMillis) {
		this.updateTextureMillis = updateTextureMillis;
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
		currentAnimTimeMillis += elapsedTime;

		if (currentAnimTimeMillis >= getUpdateTextureMillis() && animFrames > 0) {
			currentAnimTimeMillis = 0;

			final int pos = getTextPos() + 1;
			setTextPos(pos < animFrames ? pos : 0);
		}

		return ttl;
	}
}