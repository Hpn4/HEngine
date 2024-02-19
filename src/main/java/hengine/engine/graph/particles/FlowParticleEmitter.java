package hengine.engine.graph.particles;

import java.util.Iterator;

import org.joml.Vector3f;

import hengine.engine.world.item.GameItem;

public class FlowParticleEmitter extends IParticleEmitter {

	private int maxParticles;

	private boolean active;

	private boolean useColorVariation;

	private long creationPeriodMillis;

	private long lastCreationTime;

	private Vector3f positionRange;

	private float speedRndRange;

	private float scaleRndRange;

	private long animRange;

	private Vector3f startColor;

	private Vector3f endColor;

	private Vector3f colorFactor;

	public FlowParticleEmitter(final Particle baseParticle, final int maxParticles, final long creationPeriodMillis) {
		super(baseParticle);

		this.maxParticles = maxParticles;
		active = false;
		lastCreationTime = 0;
		this.creationPeriodMillis = creationPeriodMillis;

		endColor = new Vector3f(1);
		startColor = new Vector3f(1);
		colorFactor = new Vector3f(1);
		useColorVariation = false;
	}

	public void update(final long elapsedTime) {
		final long now = System.currentTimeMillis();

		if (lastCreationTime == 0)
			lastCreationTime = now;

		final Iterator<? extends GameItem> it = particles.iterator();
		while (it.hasNext()) {
			final Particle particle = (Particle) it.next();

			if (particle.updateTtl(elapsedTime) < 0)
				it.remove();
			else
				updatePosition(particle, elapsedTime);
		}

		final int length = getParticles().size();
		if (now - lastCreationTime >= creationPeriodMillis && length < maxParticles) {
			createParticle();
			lastCreationTime = now;
		}
	}

	protected void createParticle() {
		float sign = Math.random() > 0.5 ? -1.0f : 1.0f;
		final float speedX = (float) (sign * Math.random() * speedRndRange);

		sign = Math.random() > 0.5 ? -1.0f : 1.0f;
		final float speedY = (float) (sign * Math.random() * speedRndRange);

		sign = Math.random() > 0.5 ? -1.0f : 1.0f;
		final float speedZ = (float) (sign * Math.random() * speedRndRange);

		sign = Math.random() > 0.5 ? -1.0f : 1.0f;
		final float posX = (float) (sign * Math.random() * positionRange.x);

		sign = Math.random() > 0.5 ? -1.0f : 1.0f;
		final float posY = (float) (sign * Math.random() * positionRange.y);

		sign = Math.random() > 0.5 ? -1.0f : 1.0f;
		final float posZ = (float) (sign * Math.random() * positionRange.z);

		sign = Math.random() > 0.5 ? -1.0f : 1.0f;
		final float scaleInc = (float) (sign * Math.random() * scaleRndRange);

		Particle particle;
		// Si c'est une particule avec atlas
		if (getBaseParticle() instanceof AnimParticle) {
			particle = new AnimParticle((AnimParticle) getBaseParticle());
			final long updateAnimInc = (long) sign * (long) (Math.random() * (float) animRange);

			((AnimParticle) particle)
					.setUpdateTextureMills(((AnimParticle) particle).getUpdateTextureMillis() + updateAnimInc);
		} else
			particle = new Particle(getBaseParticle());

		particle.getPosition().add(posX, posY, posZ);
		particle.getSpeed().add(speedX, speedY, speedZ);
		particle.setScale(particle.getScale() + scaleInc);

		particles.add(particle);
	}

	/**
	 * Updates a particle position
	 * 
	 * @param particle    The particle to update
	 * @param elapsedTime Elapsed time in milliseconds
	 */
	public void updatePosition(final Particle particle, final long elapsedTime) {
		final float delta = elapsedTime / 1000.0f;

		final Vector3f dVec = new Vector3f(particle.getSpeed()).mul(delta);

		particle.getPosition().add(dVec);
	}

	public long getCreationPeriodMillis() {
		return creationPeriodMillis;
	}

	public void setCreationPeriodMillis(final long creationPeriodMillis) {
		this.creationPeriodMillis = creationPeriodMillis;
	}

	public int getMaxParticles() {
		return maxParticles;
	}

	public void setMaxParticles(final int maxParticles) {
		this.maxParticles = maxParticles;
	}

	public Vector3f getPositionRndRange() {
		return positionRange;
	}

	public void setPositionRndRange(final Vector3f positionRndRange) {
		this.positionRange = positionRndRange;
	}

	public float getScaleRndRange() {
		return scaleRndRange;
	}

	public void setScaleRndRange(final float scaleRndRange) {
		this.scaleRndRange = scaleRndRange;
	}

	public float getSpeedRndRange() {
		return speedRndRange;
	}

	public void setSpeedRndRange(final float speedRndRange) {
		this.speedRndRange = speedRndRange;
	}

	public void setAnimRange(final long animRange) {
		this.animRange = animRange;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(final boolean active) {
		this.active = active;
	}

	public boolean useColorVariation() {
		return useColorVariation;
	}

	public Vector3f getStartColor() {
		return startColor;
	}

	public void setStartColor(final Vector3f startColor) {
		this.startColor = startColor;
		colorFactor = endColor.sub(startColor, new Vector3f());
		useColorVariation = true;
	}

	public Vector3f getEndColor() {
		return endColor;
	}

	public void setEndColor(final Vector3f endColor) {
		this.endColor = endColor;
		colorFactor = endColor.sub(startColor, new Vector3f());
		useColorVariation = true;
	}

	public Vector3f getColorFactor() {
		return colorFactor;
	}

}
