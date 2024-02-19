package hengine.engine.graph.light;

import org.joml.Vector4f;

import hengine.engine.utils.loader.texture.AbstractTexture;

public class Material {

	public static final Vector4f DEFAULT_COLOUR = new Vector4f(1f, 1f, 1f, 1f);

	private Vector4f diffuseColour;

	private Vector4f specularColour;

	private Vector4f emissiveColour;

	private AbstractTexture diffuseMap;

	private AbstractTexture specularMap;

	private AbstractTexture emissiveMap;

	private AbstractTexture normalMap;

	private float reflectance;

	public Material() {
		diffuseColour = DEFAULT_COLOUR;
		specularColour = DEFAULT_COLOUR;
		emissiveColour = DEFAULT_COLOUR;

		diffuseMap = null;

		reflectance = 1;
	}

	// Constructeur pour les couleurs
	public Material(final Vector4f colour) {
		this(colour, 1);
	}

	public Material(final Vector4f colour, final float reflectance) {
		this(colour, colour, colour, reflectance);
	}

	public Material(final Vector4f diffuseColour, final Vector4f specularColour, final Vector4f emissiveColour,
			final float reflectance) {
		this(diffuseColour, specularColour, emissiveColour, null, null, null, null, reflectance);
	}

	// Constructeur que pour les textures
	public Material(final AbstractTexture diffuseMap, final float reflectance) {
		this(diffuseMap, null, null, null, reflectance);
	}

	public Material(final AbstractTexture diffuseMap, final AbstractTexture specularMap,
			final AbstractTexture emissiveMap, final AbstractTexture normalMap, final float reflectance) {
		this(DEFAULT_COLOUR, DEFAULT_COLOUR, DEFAULT_COLOUR, diffuseMap, specularMap, emissiveMap, normalMap,
				reflectance);
	}

	// Le gros constructeur de folie ^^
	public Material(final Vector4f diffuseColour, final Vector4f specularColour, final Vector4f emissiveColour,
			final AbstractTexture diffuseMap, final AbstractTexture specularMap, final AbstractTexture emissiveMap,
			final AbstractTexture normalMap, final float reflectance) {
		this.diffuseColour = diffuseColour;
		this.specularColour = specularColour;
		this.emissiveColour = emissiveColour;

		this.diffuseMap = diffuseMap;
		this.specularMap = specularMap;
		this.emissiveMap = emissiveMap;
		this.normalMap = normalMap;

		this.reflectance = reflectance;
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
	public Vector4f getSpecularColour() {
		return specularColour;
	}

	public void setSpecularColour(final Vector4f specularColour) {
		this.specularColour = specularColour;
	}

	/*
	 ***************************
	 ***** EMISSIVE COLOUR *****
	 ***************************
	 *
	 * The default emissive color of the material
	 */
	public Vector4f getEmissiveColour() {
		return emissiveColour;
	}

	public void setEmissiveColour(final Vector4f emissiveColour) {
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
	 **************************
	 ****** SPECULAR MAP ******
	 **************************
	 *
	 * The specular map of the material ( texture define the specular factor of each
	 * point ) The specular factor is the capacity of the material to "bright" like
	 * steel
	 */
	public AbstractTexture getSpecularMap() {
		return specularMap;
	}

	public void setSpecularMap(final AbstractTexture specularMap) {
		this.specularMap = specularMap;
	}

	/*
	 **************************
	 ****** EMISSIVE MAP ******
	 **************************
	 *
	 * The emissive map of the material ( texture define the emissive color of each
	 * point ) The emissive color is light who emit the material like led or light
	 * on a car...
	 */
	public AbstractTexture getEmissiveMap() {
		return emissiveMap;
	}

	public void setEmissiveMap(final AbstractTexture emissiveMap) {
		this.emissiveMap = emissiveMap;
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

	public float getReflectance() {
		return reflectance;
	}

	public void setReflectance(final float reflectance) {
		this.reflectance = reflectance;
	}

	public boolean hasDiffuseMap() {
		return diffuseMap != null;
	}

	public boolean hasSpecularMap() {
		return specularMap != null;
	}

	public boolean hasEmissiveMap() {
		return emissiveMap != null;
	}

	public boolean hasNormalMap() {
		return normalMap != null;
	}

	public String toString() {
		String out = "COLOUR : [" + "diffuse:" + getDiffuseColour() + ", specular:" + getSpecularColour()
				+ ", emissive:" + getEmissiveColour() + "]";
		out += "\n\treflectance:" + reflectance + ", dif map:" + hasDiffuseMap() + ", spec map:" + hasSpecularMap()
				+ ", em map:" + hasEmissiveMap() + ", normal map:" + hasNormalMap();
		return out;
	}
}
