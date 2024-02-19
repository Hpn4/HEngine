package hengine.engine.hlib.component.menu;

import hengine.engine.hlib.border.LineBorder;
import hengine.engine.hlib.component.HContainer;
import hengine.engine.hlib.event.MouseEvent;
import hengine.engine.hlib.graphics.Graphics;
import hengine.engine.hlib.graphics.paint.Color;
import hengine.engine.hlib.layout.ColumnLayout;
import hengine.engine.hlib.utils.Background;
import hengine.engine.hlib.utils.Insets;

public class HMenu extends HContainer {

	private int addX, addY;

	public HMenu() {
		setVisible(false);
		setMargin(new Insets(0));
		setMargout(new Insets(0));
		setBackground(new Background(Color.GRAY));
		setBorder(new LineBorder(Color.BLACK));
		setLayout(new ColumnLayout(1));

	}

	public void show(final int x, final int y) {
		addX = x;
		addY = y;
		setVisible(true);
	}

	public void paint(final Graphics g) {
		g.translateOrigin(addX, addY);
		super.paint(g);
		g.translateOrigin(-addX, -addY);
	}

	public void fireEvent(final MouseEvent event) {
		event.add(-addX, -addY);
		super.fireEvent(event);
		event.add(addX, addY);
	}
}
