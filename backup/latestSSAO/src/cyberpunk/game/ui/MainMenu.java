package hengine.game.ui;

import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;

import hengine.engine.hlib.component.HContainer;
import hengine.engine.hlib.component.HWindow;
import hengine.engine.hlib.component.button.HButton;
import hengine.engine.hlib.layout.RowLayout;

public class MainMenu extends HContainer {

	private final HButton quitter;
	
	private final HButton reprendre;
	
	public MainMenu(final HWindow window) {
		quitter = new HButton("Quitter");
		reprendre = new HButton("Reprendre");
		
		setLayout(new RowLayout(50));
		addAll(quitter, reprendre);
		
		quitter.addActionListener(() -> {
			glfwSetWindowShouldClose(window.getWindowHandle(), true);
		});
		
		reprendre.addActionListener(() -> {
			setVisible(false);
		});
	}
}
