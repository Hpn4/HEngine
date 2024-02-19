package hengine.engine.hlib.component.menu;

import hengine.engine.hlib.border.LineBorder;
import hengine.engine.hlib.component.button.AbstractButton;
import hengine.engine.hlib.event.MouseEvent;
import hengine.engine.hlib.graphics.Graphics;
import hengine.engine.hlib.graphics.paint.Color;
import hengine.engine.hlib.graphics.paint.Image;
import hengine.engine.hlib.graphics.paint.LinearGradient;
import hengine.engine.hlib.graphics.paint.Paint;
import hengine.engine.hlib.utils.Background;
import hengine.engine.hlib.utils.Insets;
import hengine.engine.utils.Rectangle;

public class HMenuItem extends AbstractButton {

	private boolean colorSpec;

	private Image img;

	// Vide
	public HMenuItem() {
		initListener();
	}

	// Texte
	public HMenuItem(final String txt) {
		this(txt, null, Color.BLACK);
	}

	// Texte et font
	public HMenuItem(final String txt, final int font) {
		super(txt, Color.BLACK, font);
		initListener();
	}

	// Texte image et couleur
	public HMenuItem(final String txt, final String img, final Paint color) {
		super(txt);
		setForeground(color);
		if (img != null)
			setImage(img);
		if (color != null)
			colorSpec = true;
		initListener();
	}

	// Texte et couleur
	public HMenuItem(final String txt, final Paint color) {
		this(txt, null, color);
	}

	// Texte et image
	public HMenuItem(final String txt, final String img) {
		this(txt, img, null);
	}

	public void cleanUp(final long ctx) {
		super.cleanUp(ctx);
		img.cleanUp(ctx);
	}

	public void mousePressed(final MouseEvent e) {
		super.mousePressed(e);
		color(isEnabled() ? Color.GRAY : colorDisabled);
	}

	public void mouseReleased(final MouseEvent e) {
		super.mouseReleased(e);
		color(isEnabled() ? Color.YELLOW : colorDisabled);
		// doClick();
	}

	public void mouseEntered(final MouseEvent e) {
		super.mouseEntered(e);
		color(isEnabled() ? Color.YELLOW : colorDisabled);
	}

	public void mouseExited(final MouseEvent e) {
		super.mouseExited(e);
		color(isEnabled() ? colorDefault : colorDisabled);
	}

	private void initListener() {
		setBackground(
				new Background(new LinearGradient(this, new Color(255, 238, 0, 30), new Color(156, 138, 61, 30))));
		setBorder(new LineBorder(colorDefault));
		setMargout(new Insets(0));

		if (!colorSpec)
			setForeground(colorDefault);

		setRecalcSizeNeeded(true);
	}

	private void color(final Color col) {
		getBorder().setPaint(col);
		if (!colorSpec)
			setForeground(col);
	}

	// Redefinir les icone le texte et la couleur
	public void setImage(final String image) {
		if (!image.equals(""))
			img = new Image(image);
		setRecalcSizeNeeded(true);
	}

	public void setEnabled(final boolean choix) {
		super.setEnabled(choix);
		color(choix ? colorDefault : colorDisabled);
	}

	public void initSize(final Graphics g) {
		final Rectangle rect = g.getFontBounds(0, 0, getText());
		int height = rect.height, width = rect.width;

		if (img != null) {
			width += img.getWidth() + 2;
			height = Math.max(height, img.getHeight()) + 4;
		}

		width += getAllInsetsWidth();
		height += getAllInsetsHeight();

		setSize(width, height);
	}

	public void paintComponent(final Graphics g) {
		final String txt = getText();
		final Rectangle rect = g.getFontBounds(0, 0, txt);
		final int fontWidth = rect.width, height = getPaintHeight(), fontHeight = rect.height;

		int x, y;

		if (img != null) {
			x = (getWidth() - img.getWidth() - fontWidth) / 2;
			y = (height - img.getHeight()) / 2;
			g.drawImage(img, x, y);

			y = (height - fontHeight) / 2;
			x += img.getWidth() + 2;
		} else {
			x = (getWidth() - fontWidth) / 2;
			y = (height - fontHeight + 14) / 2;
		}

		g.setPaint(getForeground());
		g.drawText(x, y, txt);

	}
}
