package hengine.engine;

import hengine.engine.hlib.component.HWindow;

public interface IGameLogic {

	void init(final HWindow window) throws Exception;

	void input(final HWindow window, final MouseInput mouseInput);

	void update(final float interval, final MouseInput mouseInput, final HWindow window);

	void render(final HWindow window);

	void cleanup(final HWindow window);

}