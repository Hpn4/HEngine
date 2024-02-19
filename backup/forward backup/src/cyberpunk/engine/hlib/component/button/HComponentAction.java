package hengine.engine.hlib.component.button;

import hengine.engine.hlib.component.HComponent;

@FunctionalInterface
public interface HComponentAction {

	public void action(final HComponent c);
}
