package hengine.engine.hlib.component.button;

import hengine.engine.hlib.event.MouseEvent;
import hengine.engine.hlib.graphics.Graphics;
import hengine.engine.hlib.graphics.paint.Color;
import hengine.engine.hlib.graphics.paint.Paint;
import hengine.engine.utils.Rectangle;

public class HCheckBox extends CheckableButton {

	private Paint colCheck = colorDefault;

	/**
	 ************************************
	 ************ CONSTRUCTOR ***********
	 ************************************
	 */
	public HCheckBox(final boolean selected) {
		this(selected, "");
	}

	/**
	 * Create a checkbox unchecked, with {@code text} for the text to be rendered
	 * 
	 * @param text
	 */
	public HCheckBox(final String text) {
		this(false, text);
	}

	// Constructeur pour le texte et la taille de police
	public HCheckBox(final boolean selected, final String text) {
		this(selected, text, Color.BLACK, 17);
	}

	// Constructeur pour le texte, la couleur et la taille de police
	public HCheckBox(final boolean select, final String text, final Paint fg, final int p) {
		super(select, text, fg, p);
		colCheck = colorDefault;
	}

	public void cleanUp(final long ctx) {
		super.cleanUp(ctx);
		colCheck.cleanUp(ctx);
	}

	/**
	 ******************************
	 ************ EVENT ***********
	 ******************************
	 */
	public void mousePressed(final MouseEvent e) {
		super.mousePressed(e);
		colCheck = colorPressed;
	}

	public void mouseReleased(final MouseEvent e) {
		super.mouseReleased(e);
		colCheck = colorEntered;
	}

	public void mouseEntered(final MouseEvent e) {
		super.mouseEntered(e);
		colCheck = colorEntered;
	}

	public void mouseExited(final MouseEvent e) {
		super.mouseExited(e);
		colCheck = colorDefault;
	}

	/**
	 ******************************
	 ************ PAINT ***********
	 ******************************
	 */
	public void paintComponent(final Graphics g) {
		final String txt = getText();
		final Rectangle rect = g.getFontBounds(0, 0, txt);
		final int x = 37, y = getHeight() / 2, yStart = 10;

		g.setPaint(colCheck);
		g.fillRoundRect(10, y - yStart, 25, 25, 5);

		g.setPaint(Color.BLACK);
		g.fillRoundRect(11, y - yStart + 1, 23, 23, 5);

		if (isSelected()) {
			g.setPaint(colCheck);
			g.fillRoundRect(14, y - yStart + 4, 17, 17, 5);
		}

		g.setPaint(getForeground());
		g.drawText(x, (y + rect.height) / 2 - yStart, txt);
	}

	public void initSize(final Graphics g) {
		final Rectangle rect = g.getFontBounds(0, 0, getText());
		int height = rect.height, width = rect.width;

		height += getAllInsetsHeight();
		width += getAllInsetsWidth();

		setSize(width, height);
	}
}
