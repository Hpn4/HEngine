package hengine.engine.graph.particles;

import org.joml.Vector3f;

import hengine.engine.graph.mesh.AbstractMesh;
import hengine.engine.utils.loader.texture.Texture;

public class AnimParticle extends Particle {

	private long updateTextureMillis;

	private long currentAnimTimeMillis;

	private final int animFrames;

	public AnimParticle(final AbstractMesh mesh, final Vector3f speed, final long ttl, final long updateTextureMillis) {
		super(mesh, speed, ttl);

		this.updateTextureMillis = updateTextureMillis;
		currentAnimTimeMillis = 0;

		final Texture texture = (Texture) getMesh().getMaterial().getDiffuseMap();
		animFrames = texture.getNumCols() * texture.getNumRows();
	}

	public AnimParticle(final AnimParticle baseParticle) {
		super(baseParticle);

		updateTextureMillis = baseParticle.updateTextureMillis;
		currentAnimTimeMillis = 0;
		animFrames = baseParticle.animFrames;
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

	/**
	 * Updates the Particle's TTL
	 * 
	 * @param elapsedTime Elapsed Time in milliseconds
	 * @return The Particle's TTL
	 */
	public long updateTtl(final long elapsedTime) {
		currentAnimTimeMillis += elapsedTime;

		if (currentAnimTimeMillis >= getUpdateTextureMillis() && animFrames > 0) {
			currentAnimTimeMillis = 0;

			final int pos = getTextPos() + 1;
			setTextPos(pos < animFrames ? pos : 0);
		}

		return super.updateTtl(elapsedTime);
	}
}