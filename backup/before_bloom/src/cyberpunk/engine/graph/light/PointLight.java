package hengine.engine.graph.light;

import org.joml.Vector3f;

public class PointLight {

	private Vector3f color;

	private Vector3f position;

	protected float intensity;

	private Attenuation attenuation;
	
	public PointLight(final Vector3f color, final Vector3f position, final float intensity) {
		attenuation = new Attenuation(1, 0, 0);
		this.color = color;
		this.position = position;
		this.intensity = intensity;
	}

	public PointLight(final Vector3f color, final Vector3f position, final float intensity, final Attenuation attenuation) {
		this(color, position, intensity);
		this.attenuation = attenuation;
	}

	public PointLight(final PointLight pointLight) {
		this(new Vector3f(pointLight.getColor()), new Vector3f(pointLight.getPosition()), pointLight.getIntensity(),
				pointLight.getAttenuation());
	}

	public Vector3f getColor() {
		return color;
	}

	public void setColor(final Vector3f color) {
		this.color = color;
	}

	public Vector3f getPosition() {
		return position;
	}

	public void setPosition(final Vector3f position) {
		this.position = position;
	}

	public float getIntensity() {
		return intensity;
	}

	public void setIntensity(final float intensity) {
		this.intensity = intensity;
	}

	public Attenuation getAttenuation() {
		return attenuation;
	}

	public void setAttenuation(final Attenuation attenuation) {
		this.attenuation = attenuation;
	}
}