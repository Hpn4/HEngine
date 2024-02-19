package hengine.engine.graph.light;

import org.joml.Vector4f;

import hengine.engine.graph.AbstractTexture;

public class Material {

	public static final Vector4f DEFAULT_COLOUR = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);

	private Vector4f ambientColour;

	private Vector4f diffuseColour;

	private Vector4f specularColour;

	private float reflectance;

	private AbstractTexture texture;

	private AbstractTexture normalMap;

	public Material() {
		ambientColour = DEFAULT_COLOUR;
		diffuseColour = DEFAULT_COLOUR;
		specularColour = DEFAULT_COLOUR;
		texture = null;
		reflectance = 0;
	}

	public Material(final Vector4f colour, final float reflectance) {
		this(colour, colour, colour, null, reflectance);
	}

	public Material(final AbstractTexture texture) {
		this(DEFAULT_COLOUR, DEFAULT_COLOUR, DEFAULT_COLOUR, texture, 0);
	}

	public Material(final AbstractTexture texture, final float reflectance) {
		this(DEFAULT_COLOUR, DEFAULT_COLOUR, DEFAULT_COLOUR, texture, reflectance);
	}

	public Material(final Vector4f ambientColour, final Vector4f diffuseColour, final Vector4f specularColour,
			final float reflectance) {
		this(ambientColour, diffuseColour, specularColour, null, reflectance);
	}

	public Material(final Vector4f ambientColour, final Vector4f diffuseColour, final Vector4f specularColour,
			final AbstractTexture texture, final float reflectance) {
		this.ambientColour = ambientColour;
		this.diffuseColour = diffuseColour;
		this.specularColour = specularColour;
		this.texture = texture;
		this.reflectance = reflectance;
	}

	public Vector4f getAmbientColour() {
		return ambientColour;
	}

	public void setAmbientColour(final Vector4f ambientColour) {
		this.ambientColour = ambientColour;
	}

	public Vector4f getDiffuseColour() {
		return diffuseColour;
	}

	public void setDiffuseColour(final Vector4f diffuseColour) {
		this.diffuseColour = diffuseColour;
	}

	public Vector4f getSpecularColour() {
		return specularColour;
	}

	public void setSpecularColour(final Vector4f specularColour) {
		this.specularColour = specularColour;
	}

	public float getReflectance() {
		return reflectance;
	}

	public void setReflectance(final float reflectance) {
		this.reflectance = reflectance;
	}

	public boolean isTextured() {
		return this.texture != null;
	}

	public AbstractTexture getTexture() {
		return texture;
	}

	public void setTexture(final AbstractTexture texture) {
		this.texture = texture;
	}

	public boolean hasNormalMap() {
		return this.normalMap != null;
	}

	public AbstractTexture getNormalMap() {
		return normalMap;
	}

	public void setNormalMap(final AbstractTexture normalMap) {
		this.normalMap = normalMap;
	}

	public String toString() {
		return "ambientC:" + getAmbientColour().toString() + ", duffuseC:" + getDiffuseColour() + ", specularC:"
				+ getSpecularColour() + ", reflectance:" + getReflectance() + ", texture:" + getTexture();
	}
}
