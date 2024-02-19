package hengine.game.ui;

import static org.lwjgl.glfw.GLFW.GLFW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_DISABLED;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_NORMAL;
import static org.lwjgl.glfw.GLFW.glfwSetInputMode;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;

import hengine.engine.hlib.component.HContainer;
import hengine.engine.hlib.component.HWindow;
import hengine.engine.hlib.component.button.HButton;
import hengine.engine.hlib.graphics.paint.Color;
import hengine.engine.hlib.layout.BorderLayout;
import hengine.engine.hlib.layout.ColumnLayout;
import hengine.engine.hlib.utils.Alignement;
import hengine.engine.hlib.utils.Background;

public class MainMenu extends HContainer {

	private final HButton quitter;

	private final HButton option;

	private final HButton reprendre;

	private final HContainer menu;
	
	private final OptionsMenu options;

	private boolean menuOpen;

	public MainMenu(final HWindow window) {
		options = new OptionsMenu();
		
		quitter = new HButton("Quitter");
		option = new HButton("Option");
		reprendre = new HButton("Reprendre");

		quitter.setFontHeight(100);
		option.setFontHeight(100);
		reprendre.setFontHeight(100);

		menu = new HContainer();
		menu.setLayout(new ColumnLayout(50, true, Alignement.CENTER, Alignement.CENTER));
		menu.noMarge();

		menu.addAll(quitter, option, reprendre);

		window.noMarge();
		noMarge();

		final Background bg = new Background(new Color(0.1f, 0.1f, 0.1f, 0.6f));
		setBackground(bg);

		setLayout(new BorderLayout());
		addComp(menu, Alignement.CENTER);

		quitter.addActionListener(() -> {
			glfwSetWindowShouldClose(window.getWindowHandle(), true);
		});

		reprendre.addActionListener(() -> {
			glfwSetInputMode(window.getWindowHandle(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);
			setVisible(false);
			menuOpen = false;
		});
		
		option.addActionListener(() -> {
			options.doLayout();
			options.open();
			removeComp(menu);
			addComp(options, Alignement.CENTER);
		});
		
	}

	public void openMenu(final HWindow window) {
		if (!menuOpen) {
			menuOpen = true;
			glfwSetInputMode(window.getWindowHandle(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);
			setVisible(true);
		} else
			reprendre.doClick();
	}

	public boolean isMenuOpen() {
		return menuOpen;
	}
}
