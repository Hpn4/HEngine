package hengine.engine.graph.anime;

import java.util.Map;
import java.util.Optional;

import hengine.engine.graph.mesh.AnimatedMesh;
import hengine.engine.world.item.GameItem;

public class AnimGameItem extends GameItem {

	private Map<String, Animation> animations;

	private Animation currentAnimation;

	public AnimGameItem(final AnimatedMesh[] meshes, final Map<String, Animation> animations) {
		super(meshes);
		this.animations = animations;
		final Optional<Map.Entry<String, Animation>> entry = animations.entrySet().stream().findFirst();
		currentAnimation = entry.isPresent() ? entry.get().getValue() : null;
	}

	public AnimGameItem(final AnimGameItem animItem) {
		super(animItem);
		
		animations = animItem.getAnimations();
		currentAnimation = animItem.getCurrentAnimation();
	}

	public Map<String, Animation> getAnimations() {
		return animations;
	}

	public Animation getAnimation(final String name) {
		return animations.get(name);
	}

	public Animation getCurrentAnimation() {
		return currentAnimation;
	}

	public void setCurrentAnimation(final Animation currentAnimation) {
		this.currentAnimation = currentAnimation;
	}
}