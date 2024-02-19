package hengine.engine.utils.loader.anim;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;

public class Node {

	private final List<Node> children;

	private final List<Matrix4f> transformations;

	private final String name;

	private final Node parent;

	public Node(final String name, final Node parent) {
		this.name = name;
		this.parent = parent;
		transformations = new ArrayList<>();
		children = new ArrayList<>();
	}

	public static Matrix4f getParentTransforms(final Node node, final int framePos) {
		if (node == null)
			return new Matrix4f();

		final Matrix4f parentTransform = new Matrix4f(getParentTransforms(node.getParent(), framePos));
		final List<Matrix4f> transformations = node.getTransformations();
		Matrix4f nodeTransform;
		int transfSize = transformations.size();

		if (framePos < transfSize)
			nodeTransform = transformations.get(framePos);
		else if (transfSize > 0)
			nodeTransform = transformations.get(transfSize - 1);
		else
			nodeTransform = new Matrix4f();

		return parentTransform.mul(nodeTransform);
	}

	public void addChild(final Node node) {
		children.add(node);
	}

	public void addTransformation(final Matrix4f transformation) {
		transformations.add(transformation);
	}

	public Node findByName(final String targetName) {
		if (name.equals(targetName))
			return this;

		Node result = null;
		for (final Node child : children) {
			result = child.findByName(targetName);
			if (result != null)
				break;
		}
		return result;
	}

	public int getAnimationFrames() {
		int numFrames = transformations.size();

		for (final Node child : children)
			numFrames = Math.max(numFrames, child.getAnimationFrames());
		return numFrames;
	}

	public List<Node> getChildren() {
		return children;
	}

	public List<Matrix4f> getTransformations() {
		return transformations;
	}

	public String getName() {
		return name;
	}

	public Node getParent() {
		return parent;
	}
}
