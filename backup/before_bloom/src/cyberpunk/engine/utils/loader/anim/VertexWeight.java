package hengine.engine.utils.loader.anim;

public class VertexWeight {

	private final int boneId;

	private int vertexId;

	private float weight;

	public VertexWeight(final int boneId, final int vertexId, final float weight) {
		this.boneId = boneId;
		this.vertexId = vertexId;
		this.weight = weight;
	}

	public int getBoneId() {
		return boneId;
	}

	public int getVertexId() {
		return vertexId;
	}

	public float getWeight() {
		return weight;
	}

	public void setVertexId(final int vertexId) {
		this.vertexId = vertexId;
	}

	public void setWeight(final float weight) {
		this.weight = weight;
	}
}
