package hengine.engine.world;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

import hengine.engine.graph.light.DirectionalLight;
import hengine.engine.graph.light.PointLight;
import hengine.engine.graph.light.SpotLight;

public class SceneLight {

	private Vector3f ambientLight;

	private Vector3f skyBoxLight;

	private final List<PointLight> pointLights;

	private final List<SpotLight> spotLights;

	private DirectionalLight directionalLight;

	public SceneLight() {
		pointLights = new ArrayList<>();
		spotLights = new ArrayList<>();
	}

	public Vector3f getSkyBoxLight() {
		return skyBoxLight;
	}

	public void setSkyBoxLight(final Vector3f skyBoxLight) {
		this.skyBoxLight = skyBoxLight;
	}

	public Vector3f getAmbientLight() {
		return ambientLight;
	}

	public void setAmbientLight(final Vector3f ambientLight) {
		this.ambientLight = ambientLight;
	}

	public List<PointLight> getPointLights() {
		return pointLights;
	}

	public void addPointLight(final PointLight pointLight) {
		pointLights.add(pointLight);
	}

	public List<SpotLight> getSpotLights() {
		return spotLights;
	}

	public void addSpotLight(final SpotLight spotLight) {
		spotLights.add(spotLight);
	}

	public DirectionalLight getDirectionalLight() {
		return directionalLight;
	}

	public void setDirectionalLight(final DirectionalLight directionalLight) {
		this.directionalLight = directionalLight;
	}

}