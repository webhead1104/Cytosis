package net.cytonic.cytosis.display;

import net.minestom.server.coordinate.Vec;
import org.joml.Quaterniond;
import org.joml.Quaternionf;

public class QuaternionUtils {

    public static float[] decompose(Quaternionf quaternion) {
        return new float[]{quaternion.x, quaternion.y, quaternion.z, quaternion.w};
    }

    public static float[] decompose(Quaterniond quaternion) {
        return new float[]{(float) quaternion.x, (float) quaternion.y, (float) quaternion.z, (float) quaternion.w};
    }

    public static Quaternionf composef(float[] raw) {
        if (raw.length != 4) throw new IllegalArgumentException("There must be 4 elements to compose a quaternion!");
        return new Quaternionf(raw[0], raw[1], raw[2], raw[3]);
    }

    public static Quaterniond composed(float[] raw) {
        if (raw.length != 4) throw new IllegalArgumentException("There must be 4 elements to compose a quaternion!");
        return new Quaterniond(raw[0], raw[1], raw[2], raw[3]);
    }

    public static Quaterniond composed(Vec from, Vec to) {
        return new Quaterniond().rotationTo(VectorUtils.composed(from), VectorUtils.composed(to));
    }

    public static Quaternionf compose(Vec from, Vec to) {
        return new Quaternionf().rotationTo(VectorUtils.composef(from), VectorUtils.composef(to));
    }

}