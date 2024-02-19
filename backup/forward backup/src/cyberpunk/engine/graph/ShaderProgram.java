package hengine.engine.graph;

import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.GL_VALIDATE_STATUS;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glDeleteShader;
import static org.lwjgl.opengl.GL20.glDetachShader;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniform2f;
import static org.lwjgl.opengl.GL20.glUniform3f;
import static org.lwjgl.opengl.GL20.glUniform4f;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glValidateProgram;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import hengine.engine.graph.light.Attenuation;
import hengine.engine.graph.light.DirectionalLight;
import hengine.engine.graph.light.Material;
import hengine.engine.graph.light.PointLight;
import hengine.engine.graph.light.SpotLight;
import hengine.engine.graph.weather.Fog;
import hengine.engine.utils.Utils;

public class ShaderProgram {

	private final int programId;

	private int vertexShaderId;

	private int geometryShaderId;

	private int fragmentShaderId;

	private final Map<String, Integer> uniforms;

	public ShaderProgram(final String shaderName) throws Exception {
		this(shaderName + ".vert", null, shaderName + ".frag");
	}

	public ShaderProgram(final String vertexName, final String geometryName, final String fragmentName)
			throws Exception {
		programId = glCreateProgram();
		if (programId == 0)
			throw new Exception("Could not create Shader : " + vertexName);
		uniforms = new HashMap<>();

		final String vertexCode = Utils.loadResource("shaders/" + vertexName),
				fragmentCode = Utils.loadResource("shaders/" + fragmentName);

		vertexShaderId = createShader(vertexName, vertexCode, GL_VERTEX_SHADER);
		fragmentShaderId = createShader(fragmentName, fragmentCode, GL_FRAGMENT_SHADER);

		if (geometryName != null) {
			final String geometryCode = Utils.loadResource("shaders/" + geometryName);

			geometryShaderId = createShader(geometryName, geometryCode, GL_GEOMETRY_SHADER);
		}
		link();
	}

	public void createUniform(final String... uniformsName) throws Exception {
		for (int i = 0, c = uniformsName.length; i < c; i++)
			createUniform(uniformsName[i]);
	}

	public void createUniform(final String uniformName, final int count) throws Exception {
		for (int i = 0; i < count; i++)
			createUniform(uniformName + "[" + i + "]");
	}

	public void createUniform(final String uniformName) throws Exception {
		final int uniformLocation = glGetUniformLocation(programId, uniformName);

		if (uniformLocation < 0)
			throw new Exception("Could not find uniform:" + uniformName);
		uniforms.put(uniformName, uniformLocation);
	}

	public void createPointLightListUniform(final String uniformName, final int size) throws Exception {
		for (int i = 0; i < size; i++)
			createPointLightUniform(uniformName + "[" + i + "]");
	}

	public void createPointLightUniform(final String uniformName) throws Exception {
		createUniform(uniformName + ".colour", uniformName + ".position", uniformName + ".intensity",
				uniformName + ".att.constant", uniformName + ".att.linear", uniformName + ".att.exponent");
	}

	public void createSpotLightListUniform(final String uniformName, final int size) throws Exception {
		for (int i = 0; i < size; i++) {
			createSpotLightUniform(uniformName + "[" + i + "]");
		}
	}

	public void createSpotLightUniform(final String uniformName) throws Exception {
		createPointLightUniform(uniformName + ".pl");
		createUniform(uniformName + ".conedir", uniformName + ".cutoff");
	}

	public void createDirectionalLightUniform(final String uniformName) throws Exception {
		createUniform(uniformName + ".colour", uniformName + ".direction", uniformName + ".intensity");
	}

	public void createMaterialUniform(final String uniformName) throws Exception {
		createUniform(uniformName + ".ambient", uniformName + ".diffuse", uniformName + ".specular", uniformName + ".hasTexture",
				uniformName + ".hasNormalMap", uniformName + ".reflectance");
	}

	public void createFogUniform(final String uniformName) throws Exception {
		createUniform(uniformName + ".activeFog", uniformName + ".colour", uniformName + ".density");
	}

	/*
	 *********************
	 **** SET UNIFORM ****
	 *********************
	 */
	public void setMatrix4f(final String uniformName, final Matrix4f value) {
		// Dump the matrix into a float buffer
		try (final MemoryStack stack = MemoryStack.stackPush()) {
			glUniformMatrix4fv(uniforms.get(uniformName), false, value.get(stack.mallocFloat(16)));
		}
	}

	public void setMatrix4fAtIndex(final String uniformName, final Matrix4f value, final int index) {
		setMatrix4f(uniformName + "[" + index + "]", value);
	}

	public void setMatrices4f(final String uniformName, final Matrix4f[] matrices) {
		try (final MemoryStack stack = MemoryStack.stackPush()) {
			final int length = matrices != null ? matrices.length : 0;
			final FloatBuffer fb = stack.mallocFloat(16 * length);

			for (int i = 0; i < length; i++)
				matrices[i].get(16 * i, fb);
			glUniformMatrix4fv(uniforms.get(uniformName), false, fb);
		}
	}

	public void setInt(final String uniformName, final int value) {
		glUniform1i(uniforms.get(uniformName), value);
	}

	public void setFloat(final String uniformName, final float value) {
		glUniform1f(uniforms.get(uniformName), value);
	}

	public void setFloatAtIndex(final String uniformName, final float value, final int index) {
		setFloat(uniformName + "[" + index + "]", value);
	}

	public void setBoolean(final String uniformName, final boolean value) {
		setInt(uniformName, value ? 1 : 0);
	}

	public void setVector2f(final String uniformName, final Vector2f value) {
		setVector2f(uniformName, value.x, value.y);
	}

	public void setVector2f(final String uniformName, final float x, final float y) {
		glUniform2f(uniforms.get(uniformName), x, y);
	}

	public void setVector3f(final String uniformName, final Vector3f value) {
		glUniform3f(uniforms.get(uniformName), value.x, value.y, value.z);
	}

	public void setVector3fAtIndex(final String uniformName, final Vector3f value, final int index) {
		setVector3f(uniformName + "[" + index + "]", value);
	}

	public void setVector4f(final String uniformName, final Vector4f value) {
		glUniform4f(uniforms.get(uniformName), value.x, value.y, value.z, value.w);
	}

	/*
	 *****************************
	 **** POINT LIGHT UNIFORM ****
	 *****************************
	 */
	public void setPointLights(final String uniformName, final List<PointLight> pointLights) {
		final int numLights = pointLights.size();
		for (int i = 0; i < numLights; i++)
			setPointLightAtIndex(uniformName, pointLights.get(i), i);
	}

	public void setPointLightAtIndex(final String uniformName, final PointLight pointLight, final int pos) {
		setPointLight(uniformName + "[" + pos + "]", pointLight);
	}

	public void setPointLight(final String uniformName, final PointLight pointLight) {
		setVector3f(uniformName + ".colour", pointLight.getColor());
		setVector3f(uniformName + ".position", pointLight.getPosition());
		setFloat(uniformName + ".intensity", pointLight.getIntensity());

		final Attenuation att = pointLight.getAttenuation();
		setFloat(uniformName + ".att.constant", att.getConstant());
		setFloat(uniformName + ".att.linear", att.getLinear());
		setFloat(uniformName + ".att.exponent", att.getExponent());
	}

	/*
	 ****************************
	 **** SPOT LIGHT UNIFORM ****
	 ****************************
	 */
	public void setSpotLights(final String uniformName, final List<SpotLight> spotLights) {
		final int numLights = spotLights.size();
		for (int i = 0; i < numLights; i++)
			setSpotLightAtIndex(uniformName, spotLights.get(i), i);
	}

	public void setSpotLightAtIndex(final String uniformName, final SpotLight spotLight, final int pos) {
		setSpotLight(uniformName + "[" + pos + "]", spotLight);
	}

	public void setSpotLight(final String uniformName, final SpotLight spotLight) {
		setPointLight(uniformName + ".pl", spotLight.getPointLight());
		setVector3f(uniformName + ".conedir", spotLight.getConeDirection());
		setFloat(uniformName + ".cutoff", spotLight.getCutOff());
	}

	public void setDirectionalLight(final String uniformName, final DirectionalLight dirLight) {
		setVector3f(uniformName + ".colour", dirLight.getColor());
		setVector3f(uniformName + ".direction", dirLight.getDirection());
		setFloat(uniformName + ".intensity", dirLight.getIntensity());
	}

	public void setMaterial(final String uniformName, final Material material) {
		setVector4f(uniformName + ".ambient", material.getAmbientColour());
		setVector4f(uniformName + ".diffuse", material.getDiffuseColour());
		setVector4f(uniformName + ".specular", material.getSpecularColour());
		setBoolean(uniformName + ".hasTexture", material.isTextured());
		setBoolean(uniformName + ".hasNormalMap", material.hasNormalMap());
		setFloat(uniformName + ".reflectance", material.getReflectance());
	}

	/**
	 * Définie l'uniform {@code uniformName} a l'objet Fog {@code fog}
	 * 
	 * @param uniformName Le nom de l'uniforme
	 * @param fog         L'objet fog a passer au shaders
	 */
	public void setFog(final String uniformName, final Fog fog) {
		setBoolean(uniformName + ".activeFog", fog.isActive());
		setVector3f(uniformName + ".colour", fog.getColour());
		setFloat(uniformName + ".density", fog.getDensity());
	}

	protected int createShader(final String fileName, final String shaderCode, final int shaderType) throws Exception {
		final int shaderId = glCreateShader(shaderType);
		if (shaderId == 0)
			throw new Exception("Error creating shader. Type: " + shaderType);

		glShaderSource(shaderId, shaderCode);
		glCompileShader(shaderId);

		if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0) {
			System.err.println("Error in shader file : " + fileName);
			throw new Exception("Error compiling Shader code: " + glGetShaderInfoLog(shaderId, 1024));
		}

		glAttachShader(programId, shaderId);

		return shaderId;
	}

	/**
	 * Attache les deux shaders au programme
	 * 
	 * @throws Exception
	 */
	public void link() throws Exception {
		glLinkProgram(programId);
		if (glGetProgrami(programId, GL_LINK_STATUS) == 0)
			throw new Exception("Error linking Shader code: " + glGetProgramInfoLog(programId, 1024));

		if (vertexShaderId != 0)
			glDetachShader(programId, vertexShaderId);

		if (geometryShaderId != 0)
			glDetachShader(programId, geometryShaderId);

		if (fragmentShaderId != 0)
			glDetachShader(programId, fragmentShaderId);

		glValidateProgram(programId);
		if (glGetProgrami(programId, GL_VALIDATE_STATUS) == 0)
			System.err.println("Warning validating Shader code: " + glGetProgramInfoLog(programId, 1024));
	}

	/**
	 * Place en mémoire le programme donc les deux shaders
	 */
	public void bind() {
		glUseProgram(programId);
	}

	/*
	 * Vide de la mémoire les deux shaders
	 */
	public void unbind() {
		glUseProgram(0);
	}

	/*
	 * Supprime le programme
	 */
	public void cleanup() {
		unbind();
		if (programId != 0)
			glDeleteProgram(programId);

		if (vertexShaderId != 0)
			glDeleteShader(vertexShaderId);

		if (geometryShaderId != 0)
			glDeleteShader(geometryShaderId);

		if (fragmentShaderId != 0)
			glDeleteShader(fragmentShaderId);
	}
}
