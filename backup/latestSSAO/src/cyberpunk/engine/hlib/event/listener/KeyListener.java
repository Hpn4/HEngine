package hengine.engine.hlib.event.listener;

import hengine.engine.hlib.event.KeyEvent;

@FunctionalInterface
public interface KeyListener extends EventListener {

	public void fire(final KeyEvent key);
}
