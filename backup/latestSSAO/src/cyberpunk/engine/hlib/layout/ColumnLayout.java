package hengine.engine.hlib.layout;

import java.util.ArrayList;

import hengine.engine.hlib.component.HComponent;
import hengine.engine.hlib.component.HContainer;
import hengine.engine.hlib.utils.Alignement;

public class ColumnLayout implements LayoutManager {

	private int yGap;
	private Alignement alignX;

	public ColumnLayout(final int yGap) {
		this(yGap, Alignement.EQUAL);
	}

	public ColumnLayout(final int yGap, final Alignement alignX) {
		setYGap(yGap);
		setAlignX(alignX);
	}

	public void setYGap(final int yGap) {
		this.yGap = yGap;
	}

	public void setAlignX(final Alignement alignX) {
		this.alignX = alignX;
	}

	public void doLayout(final HContainer cont) {
		final ArrayList<HComponent> comps = cont.getComponents();
		int x = 0, y = cont.getPaintY();

		final int startX = cont.getPaintX(), width = cont.getWidth();
		int maxWidth = 0;

		for (int i = 0, count = comps.size(); i < count; i++) {
			final HComponent comp = comps.get(i);
			final int widthComp = comp.getWidth(), heightComp = comp.getHeight();

			switch (alignX) {
			case CENTER:
				x = (width - widthComp) / 2;
				break;
			case RIGHT:
				x = width - widthComp;
				break;
			case LEFT:
				break;
			default:
				x = startX;
				break;
			}

			maxWidth = Math.max(maxWidth, widthComp);
			comp.setBounds(startX + x, y, widthComp, heightComp);
			y += heightComp + yGap;
		}

		if (alignX == Alignement.EQUAL) {
			y = cont.getPaintY();
			for (int i = 0, count = comps.size(); i < count; i++) {
				final HComponent comp = comps.get(i);
				final int heightComp = comp.getHeight();

				comp.setBounds(startX, y, maxWidth, heightComp);
				y += heightComp + yGap;
			}
		}

		cont.setWidth(maxWidth + cont.getAllInsetsWidth());
		cont.setHeight(y + cont.getAllInsetsHeight());
	}
}
