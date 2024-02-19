package hengine.engine.graph.anime;

import java.util.List;

public class Animation {

	private int currentFrame;

	private List<AnimatedFrame> frames;

	private String name;

	private double duration;

	public Animation(final String name, final List<AnimatedFrame> frames, final double duration) {
		this.name = name;
		this.frames = frames;
		currentFrame = 0;
		this.duration = duration;
	}

	public AnimatedFrame getCurrentFrame() {
		return frames.get(currentFrame);
	}

	public double getDuration() {
		return duration;
	}

	public List<AnimatedFrame> getFrames() {
		return frames;
	}

	public String getName() {
		return name;
	}

	public AnimatedFrame getNextFrame() {
		nextFrame();
		return frames.get(currentFrame);
	}

	public void nextFrame() {
		final int nextFrame = currentFrame + 1;
		if (nextFrame > frames.size() - 1)
			currentFrame = 0;
		else
			currentFrame = nextFrame;
	}
}