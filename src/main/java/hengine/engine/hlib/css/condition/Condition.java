package hengine.engine.hlib.css.condition;

import java.io.Serializable;

import hengine.engine.hlib.component.HComponent;

public interface Condition extends Serializable {

	public boolean match(final HComponent comp);
}
