package hengine.engine.hlib.css.condition;

import java.util.ArrayList;
import java.util.Objects;

import hengine.engine.hlib.component.HComponent;

public class CSSCondition {

	private final ArrayList<Condition> conditions;

	public CSSCondition() {
		conditions = new ArrayList<>();
	}

	public CSSCondition(final String cond) {
		conditions = new ArrayList<>();

		if (cond != null) {
			final String[] statements = cond.split(",");
			for (final String line : statements) {
				final int index = line.indexOf("=");
				final String key = line.substring(0, index);
				final String statement = line.substring(index + 1);

				Condition condition = null;
				switch (key) {
				case "id":
					condition = new IdCondition(statement);
					break;
				}

				if (condition != null)
					conditions.add(condition);
			}
		}
	}

	public CSSCondition id(final String id, final StringOperator op) {
		return addGeneric(new IdCondition(id, op));
	}

	public CSSCondition addGeneric(final Condition condition) {
		conditions.add(condition);
		return this;
	}

	public boolean match(final HComponent comp) {
		if (conditions.isEmpty())
			return true;

		for (final Condition condition : conditions)
			if (!condition.match(comp))
				return false;

		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hash(conditions);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof CSSCondition))
			return false;

		final CSSCondition other = (CSSCondition) obj;
		return Objects.equals(conditions, other.conditions);
	}

}
