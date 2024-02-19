package hengine.engine.hlib.css;

import hengine.engine.hlib.component.HComponent;
import hengine.engine.hlib.css.condition.CSSCondition;

public class CSSData {

	private final CSSEvent event;

	private final CSSCondition condition;

	private final HComponentStyle style;

	public CSSData(final CSSEvent event, final String condition, final HComponentStyle style) {
		this.event = event;
		this.condition = new CSSCondition(condition);
		this.style = style;
	}

	public CSSEvent getEvent() {
		return event;
	}

	public boolean match(final HComponent comp) {
		return condition.match(comp);
	}

	public CSSCondition getCondition() {
		return condition;
	}

	public HComponentStyle getStyle() {
		return style;
	}
}
