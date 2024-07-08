package net.slqmy.parrot_mail.util;

import net.minecraft.util.Mth;
import org.bukkit.util.Vector;

public final class MathUtils {
    public static final double RADIANS_TO_DEGREES = 180.0D / (float) Math.PI;

    public static Vector getFacingDirection(float yaw, float pitch) {
        Vector vector = new Vector();

        double xz = Math.cos(Math.toRadians(pitch));

        vector.setX(-xz * Math.sin(Math.toRadians(yaw)));
        vector.setZ(xz * Math.cos(Math.toRadians(yaw)));

        return vector;
    }

    public static float toNormalDegrees(float minecraftDegrees) {
        float angle = 180 - minecraftDegrees;
        if (angle < 0) {
            angle += 360;
        }

        return angle;
    }

    public static float lerpRotation(float start, float end, float factor) {
        start = toMinecraftDegrees(start);
        end = toMinecraftDegrees(end);

        return start + smallestAngleDifference(start, end) * factor;
    }

    public static float toMinecraftDegrees(float degrees) {
        return Mth.wrapDegrees(degrees);
    }

    public static float smallestAngleDifference(float start, float end) {
        float normalDiff = Math.abs(end - start);
        float altDiff = 360 - Math.abs(start) - Math.abs(end);

        return Math.min(normalDiff, altDiff) * Math.signum(toMinecraftDegrees(end - start));
    }

    public static float limitRot(float value, float limit, float rotationDirection) {
        if (rotationDirection == 0) {
            return Float.NaN;
        } else if (rotationDirection > 0) {
            return Math.max(value + 180.0F, limit + 180.0F) - 180.0F;
        } else {
            return Math.min(value + 180.0F, limit + 180.0F) - 180.0F;
        }
    }

    public static float clampHeadYawToBody(float headYaw, float bodyYaw) {
        float diff = toMinecraftDegrees(headYaw - bodyYaw);

        if (diff > 50.0F) {
            return bodyYaw + 50.0F;
        } else if (diff < -50.0F) {
            return bodyYaw - 50.0F;
        }

        return headYaw;
    }
}
