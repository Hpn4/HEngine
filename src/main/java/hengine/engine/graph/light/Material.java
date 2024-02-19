package hengine.engine.graph.light;

import org.joml.Vector3f;
import org.joml.Vector4f;

import hengine.engine.utils.loader.texture.AbstractTexture;

public class Material {

	public static final Vector4f DEFAULT_COLOUR = new Vector4f(1f, 1f, 1f, 1f);

	public static final Vector3f EMISSIVE_DEFAULT_COLOUR = new Vector3f(0f, 0f, 0f);

	private Vector4f diffuseColour;

	private float specularFactor;

	private Vector3f emissiveColour;

	private AbstractTexture diffuseMap;

	private AbstractTexture normalMap;

	private float shininess;

	private final boolean isTransparent;

	private final boolean hasSpecularMap;

	private final boolean hasEmissiveMap;

	public Material() {
		diffuseColour = DEFAULT_COLOUR;
		specularFactor = 1;
		emissiveColour = EMISSIVE_DEFAULT_COLOUR;

		isTransparent = hasSpecularMap = hasEmissiveMap = false;

		shininess = 1;
	}

	public Material(final AbstractTexture diffuseMap, final float shininess) {
		this();
		
		this.diffuseMap = diffuseMap;
		this.shininess = shininess;
	}
	public Material(final Vector4f diffuseColour, final float specularFactor, final Vector3f emissiveColour,
			final AbstractTexture diffuseMap, final AbstractTexture normalMap, final float shininess,
			final boolean isTransparent, final boolean hasSpecularMap, final boolean hasEmissiveMap) {
		this.diffuseColour = diffuseColour;
		this.specularFactor = specularFactor;
		this.emissiveColour = emissiveColour;

		this.diffuseMap = diffuseMap;
		this.normalMap = normalMap;

		this.shininess = shininess;

		this.isTransparent = isTransparent;

		this.hasSpecularMap = hasSpecularMap;
		this.hasEmissiveMap = hasEmissiveMap;
	}

	/*
	 **************************
	 ***** DIFFUSE COLOUR *****
	 **************************
	 *
	 * The default color diffuse by the material
	 */
	public Vector4f getDiffuseColour() {
		return diffuseColour;
	}

	public void setDiffuseColour(final Vector4f diffuseColour) {
		this.diffuseColour = diffuseColour;
	}

	/*
	 ***************************
	 ***** SPECULAR COLOUR *****
	 ***************************
	 *
	 * The default specular color of the material
	 */
	public float getSpecularFactor() {
		return specularFactor;
	}

	public void setSpecularFactor(final float specularFactor) {
		this.specularFactor = specularFactor;
	}

	/*
	 ***************************
	 ***** EMISSIVE COLOUR *****
	 ***************************
	 *
	 * The default emissive color of the material
	 */
	public Vector3f getEmissiveColour() {
		return emissiveColour;
	}

	public void setEmissiveColour(final Vector3f emissiveColour) {
		this.emissiveColour = emissiveColour;
	}

	/*
	 *************************
	 ****** DIFFUSE MAP ******
	 *************************
	 *
	 * The diffuse map of the material ( it's a texture who define the diffuse color
	 * of each point of the model )
	 */
	public AbstractTexture getDiffuseMap() {
		return diffuseMap;
	}

	public void setDiffuseMap(final AbstractTexture diffuseMap) {
		this.diffuseMap = diffuseMap;
	}

	/*
	 ************************
	 ****** NORMAL MAP ******
	 ************************
	 *
	 * The normal map of the material ( texture define the normal of each point )
	 */
	public AbstractTexture getNormalMap() {
		return normalMap;
	}

	public void setNormalMap(final AbstractTexture normalMap) {
		this.normalMap = normalMap;
	}

	/*
	 ***********************
	 ****** SHININESS ******
	 ***********************
	 */
	public float getShininess() {
		return shininess;
	}

	public void setShininess(final float shininess) {
		this.shininess = shininess;
	}

	/*
	 *************************
	 ****** HAS TEXTURE ******
	 *************************
	 */
	public boolean hasDiffuseMap() {
		return diffuseMap != null;
	}

	public boolean hasSpecularMap() {
		return hasSpecularMap;
	}

	public boolean hasEmissiveMap() {
		return hasEmissiveMap;
	}

	public boolean hasNormalMap() {
		return normalMap != null;
	}

	/*
	 **************************
	 ****** TRANSPARENCY ******
	 **************************
	 */
	public boolean isTransparent() {
		return isTransparent;
	}

	public String toString() {
		String out = "diffuse :" + getDiffuseColour() + ", specularFactor:" + getSpecularFactor() + ", emissive :"
				+ getEmissiveColour();
		out += "\n\tshininess:" + shininess + ", dif map:" + hasDiffuseMap() + ", spec map:" + hasSpecularMap()
				+ ", em map:" + hasEmissiveMap() + ", normal map:" + hasNormalMap() + ", isTranaprent:" + isTransparent();
		return out;
	}
}
