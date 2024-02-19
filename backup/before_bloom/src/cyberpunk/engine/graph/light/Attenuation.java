package hengine.engine.graph.light;

public class Attenuation {

	private float constant;

	private float linear;

	private float exponent;

	public Attenuation(final float constant, final float linear, final float exponent) {
		this.constant = constant;
		this.linear = linear;
		this.exponent = exponent;
	}

	public float getConstant() {
		return constant;
	}

	public void setConstant(final float constant) {
		this.constant = constant;
	}

	public float getLinear() {
		return linear;
	}

	public void setLinear(final float linear) {
		this.linear = linear;
	}

	public float getExponent() {
		return exponent;
	}

	public void setExponent(final float exponent) {
		this.exponent = exponent;
	}
}
