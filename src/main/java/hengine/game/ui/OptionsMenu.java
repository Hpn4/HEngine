package hengine.game.ui;

import hengine.engine.graph.Options;
import hengine.engine.hlib.component.HContainer;
import hengine.engine.hlib.component.button.HButton;
import hengine.engine.hlib.component.button.HCheckBox;
import hengine.engine.hlib.layout.FlowLayout;
import hengine.engine.hlib.utils.Insets;

public class OptionsMenu extends HContainer {

	private final HButton valider;

	private final HButton retour;

	private final HCheckBox renderShadows;

	private final HCheckBox activeSSAO;

	public OptionsMenu() {
		valider = new HButton("Valider", 100);
		retour = new HButton("Retour", 100);

		renderShadows = new HCheckBox("Déssiné les ombres : ");
		renderShadows.setFontHeight(100);

		activeSSAO = new HCheckBox("Activer les SSAO : ");
		activeSSAO.setFontHeight(100);

		setMargin(new Insets(50, 50, 0, 0));
		setLayout(new FlowLayout());

		final HContainer but = new HContainer(retour, valider);
		addAll(renderShadows, activeSSAO, but);
	}

	public void open() {
		renderShadows.setSelected(Options.renderShadows);
		activeSSAO.setSelected(Options.activeSSAO);
		setVisible(true);
		this.infoPos();
	}
}
