package net.slqmy.parrot_mail.util;

import net.minecraft.util.Mth;
import org.bukkit.entity.Parrot;

public final class ParrotRotation {
    private static final float MOVEMENT_THRESHOLD = 0.0025000002F;
    private static final float MAX_HEAD_YAW_DIFF = 50.0F;

    private final net.minecraft.world.entity.animal.Parrot nmsParrot;
    private float bodyYaw;

    public ParrotRotation(Parrot parrot) {
        this.nmsParrot = Utils.toNMS(parrot);
        this.bodyYaw = nmsParrot.yBodyRot;
    }

    public float getVisualBodyYaw() {
        return bodyYaw;
    }

    /**
     * Updates the visual body yaw of the parrot based on the current location and the previous location. This method should be run every tick, using a BukkitRunnable.
     */
    public void tick() {
        double dx = nmsParrot.getX() - nmsParrot.xo;
        double dz = nmsParrot.getZ() - nmsParrot.zo;

        float dSquared = (float) (dx * dx + dz * dz);
        float newBodyRot = nmsParrot.yBodyRot;

        float targetBodyRot;

        if (dSquared > MOVEMENT_THRESHOLD) {
            targetBodyRot = (float) (Mth.atan2(dz, dx) * MathUtils.RADIANS_TO_DEGREES) - 90.0F;
            float yawDifference = Mth.abs(Mth.wrapDegrees(nmsParrot.yHeadRot) - targetBodyRot);

            if (95.0F < yawDifference && yawDifference < 265.0F) {
                newBodyRot = targetBodyRot - 180.0F;
            } else {
                newBodyRot = targetBodyRot;
            }
        }

        this.turnBody(newBodyRot);
    }

    private void turnBody(float bodyRotation) {
        float difference = Mth.wrapDegrees(bodyRotation - bodyYaw);
        this.bodyYaw += difference * 0.3F;

        float turnedDegrees = Math.abs(Mth.wrapDegrees(nmsParrot.yHeadRot - bodyYaw));
        if (turnedDegrees > MAX_HEAD_YAW_DIFF) {
            this.bodyYaw += turnedDegrees - Math.signum(turnedDegrees) * MAX_HEAD_YAW_DIFF;
        }
    }
}
