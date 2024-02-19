package hengine.engine.hlib.event.listener;

import hengine.engine.hlib.event.MouseEvent;

@FunctionalInterface
public interface MouseListener extends EventListener {

	public void fire(final MouseEvent mouse);
}
