package hengine.engine.hlib.utils;

import java.io.Serializable;

public class Insets implements Cloneable, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2741348719496331553L;
	public int top, left, bot, right;

	public Insets(final int all) {
		this(all, all, all, all);
	}

	public Insets(final int top, final int left, final int bot, final int right) {
		set(top, left, bot, right);
	}

	public void set(final int top, final int left, final int bot, final int right) {
		this.top = top;
		this.left = left;
		this.bot = bot;
		this.right = right;
	}

	public int getWidth() {
		return left + right;
	}

	public int getHeight() {
		return top + bot;
	}

	public int getTotal() {
		return getWidth() + getHeight();
	}

	public String toString() {
		return getClass().getName() + "[top:" + top + ", left:" + left + ", bot:" + bot + ", right:" + right + "]";
	}

	public Object clone() {
		try {
			return super.clone();
		} catch (final CloneNotSupportedException e) {
			throw new InternalError(e);
		}
	}

	public int hashCode() {
		int sum1 = left + bot;
		int sum2 = right + top;
		int val1 = sum1 * (sum1 + 1) / 2 + left;
		int val2 = sum2 * (sum2 + 1) / 2 + top;
		int sum3 = val1 + val2;
		return sum3 * (sum3 + 1) / 2 + val2;
	}

	public boolean equals(final Object obj) {
		if (obj instanceof Insets) {
			final Insets in = (Insets) obj;
			return in.left == left && in.top == top && in.right == right && in.bot == bot;
		}
		return false;
	}
}
