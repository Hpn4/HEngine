package hengine.engine.hlib.utils;

import hengine.engine.hlib.component.HComponent;
import hengine.engine.hlib.css.CSSParser;
import hengine.engine.hlib.event.KeyEvent;

public abstract class HLib {

	public static String fontFile;

	public static HComponent focusedComp;

	public static CSSParser cssParser;

	public static int lineWidth = 1;

	static {
		//cssParser = new CSSParser("resources/style/default.css");
		cssParser = null;
	}

	public static HComponent setFocusOn(final HComponent comp) {
		if (focusedComp != null)
			focusedComp.setFocus(false);
		comp.setFocus(true);

		final HComponent older = focusedComp;
		focusedComp = comp;
		return older;
	}

	public static void fireKeyEvent(final KeyEvent key) {
		if (focusedComp != null) {
			if (key.isPressed())
				focusedComp.keyPressed(key);
			else if (key.isReleassed())
				focusedComp.keyReleased(key);
			else if (key.isRepeated())
				focusedComp.keyTyped(key);
		}
	}
}
