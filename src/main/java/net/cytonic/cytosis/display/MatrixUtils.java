package net.cytonic.cytosis.display;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.metadata.display.AbstractDisplayMeta;
import org.joml.Matrix4d;
import org.joml.Quaterniond;
import org.joml.Vector3d;

public class MatrixUtils {

    public static Matrix4d composed(AbstractDisplayMeta meta) {
        return composed(meta.getTranslation().asVec(), meta.getScale(), meta.getRightRotation(),
            meta.getLeftRotation());
    }

    public static Matrix4d composed(Vec translation, Vec scale, float[] rr, float[] lr) {
        return composed(VectorUtils.composed(translation), VectorUtils.composed(scale), QuaternionUtils.composed(rr),
            QuaternionUtils.composed(lr));
    }

    public static Matrix4d composed(Vector3d translation, Vector3d scale, Quaterniond rr, Quaterniond lr) {
        return new Matrix4d().translate(translation).rotate(lr).scale(scale).rotate(rr);
    }

    public static Matrix4d rotateX(Matrix4d mat, Vector3d point, double rad) {
        Matrix4d rotationMatrix = new Matrix4d().rotateX(rad);
        Vector3d rotatedPoint = new Vector3d();
        mat.getTranslation(rotatedPoint);
        rotationMatrix.transformPosition(rotatedPoint);

        return mat.rotateAroundLocal(new Quaterniond().rotateX(rad), point.x(), point.y(), point.z())
            .setTranslation(rotatedPoint);
    }

    public static Matrix4d rotateX(Matrix4d mat, Point point, double degrees) {
        return rotateX(mat, VectorUtils.composed(point), Math.toRadians(degrees));
    }

    public static Matrix4d rotateY(Matrix4d mat, Vector3d point, double rad) {
        Matrix4d rotationMatrix = new Matrix4d().rotateY(rad);
        Vector3d rotatedPoint = new Vector3d();
        mat.getTranslation(rotatedPoint);
        rotationMatrix.transformPosition(rotatedPoint);

        return mat.rotateAroundLocal(new Quaterniond().rotateY(rad), point.x(), point.y(), point.z())
            .setTranslation(rotatedPoint);
    }

    public static Matrix4d rotateY(Matrix4d mat, Point point, double degrees) {
        return rotateY(mat, VectorUtils.composed(point), Math.toRadians(degrees));
    }

    public static Matrix4d rotateZ(Matrix4d mat, Vector3d point, double rad) {
        Matrix4d rotationMatrix = new Matrix4d().rotateZ(rad);
        Vector3d rotatedPoint = new Vector3d();
        mat.getTranslation(rotatedPoint);
        rotationMatrix.transformPosition(rotatedPoint);

        return mat.rotateAroundLocal(new Quaterniond().rotateZ(rad), point.x(), point.y(), point.z())
            .setTranslation(rotatedPoint);
    }

    public static Matrix4d rotateZ(Matrix4d mat, Point point, double degrees) {
        return rotateZ(mat, VectorUtils.composed(point), Math.toRadians(degrees));
    }

    public static void apply(Matrix4d mat, AbstractDisplayMeta meta) {
        meta.setTranslation(VectorUtils.decompose(mat.getTranslation(new Vector3d())));
        meta.setScale(VectorUtils.decompose(mat.getScale(new Vector3d())));
        meta.setLeftRotation(QuaternionUtils.decompose(mat.getUnnormalizedRotation(new Quaterniond())));
        meta.setRightRotation(QuaternionUtils.decompose(new Quaterniond()));
    }
}
