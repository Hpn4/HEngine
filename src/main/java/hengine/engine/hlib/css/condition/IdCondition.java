package hengine.engine.hlib.css.condition;

import java.util.Objects;

import hengine.engine.hlib.component.HComponent;

public class IdCondition implements Condition {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2874153060862684467L;

	private final String id;

	private final StringOperator op;

	public IdCondition(final String statement) {
		final boolean ends = statement.endsWith("*");
		if (statement.startsWith("*")) {
			if (ends)
				op = StringOperator.CONTAINS;
			else
				op = StringOperator.START;
		} else if (ends)
			op = StringOperator.END;
		else
			op = StringOperator.EQUAL;

		id = statement.replace("*", "");
	}

	public IdCondition(final String id, final StringOperator op) {
		this.id = id;
		this.op = op;
	}

	public boolean match(final HComponent comp) {
		switch (op) {
		case EQUAL:
			return comp.getCssId().equals(id);
		case START:
			return comp.getCssId().startsWith(id);
		case END:
			return comp.getCssId().endsWith(id);
		default:
			return comp.getCssId().contains(id);
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, op);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof IdCondition))
			return false;

		final IdCondition other = (IdCondition) obj;
		return Objects.equals(id, other.id) && op == other.op;
	}

}
