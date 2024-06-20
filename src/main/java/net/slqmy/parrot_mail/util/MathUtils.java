package net.slqmy.parrot_mail.util;

import net.minecraft.util.Mth;
import org.bukkit.util.Vector;

public final class MathUtils {
    public static final float RADIANS_TO_DEGREES = 180.0F / (float) Math.PI;

    public static Vector getFacingDirection(float yaw, float pitch) {
        Vector vector = new Vector();

        double xz = Math.cos(Math.toRadians(pitch));

        vector.setX(-xz * Math.sin(Math.toRadians(yaw)));
        vector.setZ(xz * Math.cos(Math.toRadians(yaw)));

        return vector;
    }

    public static float mcYawToAngle(float yaw) {
        float angle = 180 - yaw;
        if (angle < 0) {
            angle += 360;
        }

        return angle;
    }

    public static float lerp(float start, float end, float factor) {
        return start + (end - start) * factor;
    }

    public static float toMinecraftDegrees(float degrees) {
        return Mth.wrapDegrees(degrees);
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
