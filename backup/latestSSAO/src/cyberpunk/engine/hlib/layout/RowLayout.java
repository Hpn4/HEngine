package hengine.engine.hlib.layout;

import java.util.ArrayList;

import hengine.engine.hlib.component.HComponent;
import hengine.engine.hlib.component.HContainer;
import hengine.engine.hlib.utils.Alignement;

public class RowLayout implements LayoutManager {

	private int xGap;
	private Alignement alignY;

	public RowLayout(final int xGap) {
		this(xGap, Alignement.EQUAL);
	}

	public RowLayout(final int xGap, final Alignement alignY) {
		setXGap(xGap);
		setAlignY(alignY);
	}

	public void setXGap(final int xGap) {
		this.xGap = xGap;
	}

	public void setAlignY(final Alignement alignY) {
		this.alignY = alignY;
	}

	public void doLayout(final HContainer cont) {
		final ArrayList<HComponent> comps = cont.getComponents();
		int x = cont.getPaintX(), y = 0;

		final int startY = cont.getPaintY(), height = cont.getPaintHeight();
		int maxHeight = 0;

		for (int i = 0, count = comps.size(); i < count; i++) {
			final HComponent comp = comps.get(i);
			final int widthComp = comp.getWidth(), heightComp = comp.getHeight();

			switch (alignY) {
			case CENTER:
				y = (height - heightComp) / 2;
				break;
			case BOT:
				y = height - heightComp - cont.getAllInsetsYMax() - comp.getAllInsetsYMax();
				break;
			case TOP:
				y = 0;
				break;
			default:
				y = startY;
				break;
			}

			maxHeight = Math.max(maxHeight, heightComp);
			comp.setBounds(x, startY + y, widthComp, heightComp);
			x += widthComp + xGap;
		}

		if (alignY == Alignement.EQUAL) {
			x = cont.getPaintX();
			for (int i = 0, count = comps.size(); i < count; i++) {
				final HComponent comp = comps.get(i);
				final int widthComp = comp.getWidth();

				comp.setBounds(x, startY, widthComp, maxHeight);
				x += widthComp + xGap;
			}
		}

		cont.setHeight(maxHeight + cont.getAllInsetsWidth());
		cont.setWidth(x);
	}
}
