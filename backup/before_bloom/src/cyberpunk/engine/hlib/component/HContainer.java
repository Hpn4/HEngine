package hengine.engine.hlib.component;

import java.util.ArrayList;

import hengine.engine.hlib.component.button.HButtonGroup;
import hengine.engine.hlib.component.button.HComponentAction;
import hengine.engine.hlib.event.MouseEvent;
import hengine.engine.hlib.graphics.Graphics;
import hengine.engine.hlib.layout.LayoutManager;
import hengine.engine.hlib.utils.Alignement;

public class HContainer extends HComponent {

	private final ArrayList<HComponent> comp = new ArrayList<>();

	private LayoutManager layout;

	public HContainer() {
		super();
		setRecalcSizeNeeded(true);
	}

	public HContainer(final HComponent... components) {
		super();
		addAll(components);
		setRecalcSizeNeeded(true);
	}

	public void addComp(final HComponent h) {
		comp.add(h);
		if (isVisible())
			doLayout();
	}

	public void addComp(final HComponent h, final Alignement align) {
		comp.add(h);
		layout.addComp(h, align);
	}

	public void addAll(final HComponent... components) {
		for (int i = 0, c = components.length; i < c; i++)
			comp.add(components[i]);
		if (isVisible())
			doLayout();
	}

	public void addFromButtonGroup(final HButtonGroup bg) {
		for (int i = 0, c = bg.getButtons().size(); i < c; i++)
			addComp(bg.getButtons().get(i));
	}

	public void setVisible(final boolean visible) {
		super.setVisible(visible);

		forEach(comp -> {
			comp.setVisible(visible);
		});

		if (visible)
			doLayout();
	}

	public void paintComponent(final Graphics g) {
		forEach(comp -> {
			if (comp.isVisible()) {
				final int x = comp.getX() - getX(), y = comp.getY() - getY();
				g.translateOrigin(x, y);
				comp.paint(g);
				g.translateOrigin(-x, -y);
			}
		});
	}

	public void setSize(final int width, final int height) {
		super.setSize(width, height);
		doLayout();
	}

	public void fireEvent(final MouseEvent event) {
		super.fireEvent(event);
		forEach(comp -> {
			comp.fireEvent(event);
		});
	}

	public HComponent getComp(final int index) {
		return comp.get(index);
	}

	public int getComponentCount() {
		return comp.size();
	}

	public ArrayList<HComponent> getComponents() {
		return comp;
	}

	public void forEach(final HComponentAction action) {
		for (int i = 0, c = getComponentCount(); i < c; i++)
			action.action(getComp(i));
	}

	public void initSize(final Graphics g) {
		super.setSize(HWindow.frameWidth, HWindow.frameHeight);
		doLayout();
	}

	public void recalcSize(final Graphics g) {
		forEach(comp -> {
			if (comp instanceof HContainer)
				((HContainer) comp).recalcSize(g);
			else if (comp.isRecalcSizeNeeded()) {
				comp.initSize(g);
				comp.setRecalcSizeNeeded(false);
			}
		});

		if (isRecalcSizeNeeded()) {
			setRecalcSizeNeeded(false);
			doLayout();
		}
	}

	public void cleanUp(final long ctx) {
		super.cleanUp(ctx);
		forEach(comp -> {
			comp.cleanUp(ctx);
		});
	}

	/**
	 *************************************
	 *********** LAYOUT MANAGER **********
	 *************************************
	 */
	public void setLayout(final LayoutManager layout) {
		this.layout = layout;
	}

	public LayoutManager getLayoutManager() {
		return layout;
	}

	public void doLayout() {
		if (layout != null)
			layout.doLayout(this);
	}
}
