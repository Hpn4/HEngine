package hengine.engine.utils.loader.anim;

import org.joml.Matrix4f;

public class Bone {

    private final int boneId;

    private final String boneName;

    private final Matrix4f offsetMatrix;

    public Bone(final int boneId, final String boneName, final Matrix4f offsetMatrix) {
        this.boneId = boneId;
        this.boneName = boneName;
        this.offsetMatrix = offsetMatrix;
    }

    public int getBoneId() {
        return boneId;
    }

    public String getBoneName() {
        return boneName;
    }

    public Matrix4f getOffsetMatrix() {
        return offsetMatrix;
    }

}