package hengine.engine.hlib.layout;

import java.security.InvalidParameterException;

import hengine.engine.hlib.component.HComponent;
import hengine.engine.hlib.component.HContainer;
import hengine.engine.hlib.utils.Alignement;

public class BorderLayout implements LayoutManager {

	private HComponent left, top, right, bot, center;
	private int xGap, yGap;

	public BorderLayout() {
		this(5, 5);
	}

	public BorderLayout(final int xGap, final int yGap) {
		setGap(xGap, yGap);
	}

	public void doLayout(final HContainer cont) {
		int width = cont.getPaintWidth() - 10, height = cont.getPaintHeight(), x = cont.getPaintX(), y = cont.getPaintY();

		if (top != null) {
			final int heightComp = top.getHeight();
			top.setBounds(x, y, width, top.getHeight());
			y += heightComp + yGap;
			height -= heightComp + yGap;
		}

		if (bot != null) {
			final int heightComp = bot.getHeight();
			bot.setBounds(x, cont.getHeight() - cont.getAllInsetsYMax() - heightComp, width, bot.getHeight());
			height -= heightComp + yGap;
		}

		if (left != null) {
			final int widthComp = left.getWidth();
			left.setBounds(x, y, left.getWidth(), height);
			x += widthComp + xGap;
			width -= widthComp + xGap;
		}

		if (right != null) {
			final int widthComp = right.getWidth();
			right.setBounds(cont.getWidth() - cont.getAllInsetsXMax() - widthComp, y, right.getWidth(), height);
			width -= widthComp + xGap;
		}

		if (center != null)
			center.setBounds(x, y, width, height);
	}

	public void setGap(final int xGap, final int yGap) {
		this.xGap = xGap;
		this.yGap = yGap;
	}

	public void addComp(final HComponent comp, final Alignement align) {
		switch (align) {
		case BOT:
			bot = comp;
			break;
		case CENTER:
			center = comp;
			break;
		case LEFT:
			left = comp;
			break;
		case RIGHT:
			right = comp;
			break;
		case TOP:
			top = comp;
			break;
		default:
			throw new InvalidParameterException(
					"Can't assign this alignement: " + align + ", only accepted: LEFT, TOP, RIGHT, BOT or CENTER");
		}
	}
}
