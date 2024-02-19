package hengine.engine.hlib.layout;

import hengine.engine.hlib.component.HComponent;
import hengine.engine.hlib.component.HContainer;
import hengine.engine.hlib.utils.Alignement;

public interface LayoutManager {

	void doLayout(final HContainer cont);

	public default void addComp(final HComponent comp, final Alignement align) {

	}
}
