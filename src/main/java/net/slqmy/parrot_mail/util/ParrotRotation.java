package net.slqmy.parrot_mail.util;

import net.minecraft.util.Mth;
import org.bukkit.entity.Parrot;

import static net.slqmy.parrot_mail.util.MathUtils.toMinecraftDegrees;

public final class ParrotRotation {
    private static final double MOVEMENT_THRESHOLD = 0.0025000002D;
    private static final float MAX_YAW_DIFF_BETWEEN_HEAD_AND_BODY = 50.0F;

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
     * Sets the visual body yaw of the parrot. Useful for adjusting the yaw when confident of its real value.
     * @param bodyYaw The visual body yaw.
     */
    public void setVisualBodyYaw(float bodyYaw) {
        this.bodyYaw = toMinecraftDegrees(bodyYaw);
    }

    /**
     * Updates the visual body yaw of the parrot based on the current location and the previous location. This method should be run every tick, using a BukkitRunnable.
     */
    public void tick() {
        double dx = nmsParrot.getX() - nmsParrot.xo;
        double dz = nmsParrot.getZ() - nmsParrot.zo;
        double d2 = (dx * dx + dz * dz);

        float bodyRot = nmsParrot.yBodyRot;
        float targetBodyRot;

        if (d2 > MOVEMENT_THRESHOLD) {
            targetBodyRot = (float) (Mth.atan2(dz, dx) * MathUtils.RADIANS_TO_DEGREES) - 90.0F;
            float difference = Math.abs(toMinecraftDegrees(nmsParrot.getYRot()) - targetBodyRot);

            if (95.0F < difference && difference < 265.0F) {
                bodyRot = targetBodyRot - 180.0F;
            } else {
                bodyRot = targetBodyRot;
            }
        }

        this.turnBody(bodyRot);
    }

    private void turnBody(float bodyRotation) {
        float differenceToRealBodyYaw = toMinecraftDegrees(bodyRotation - bodyYaw);
        this.bodyYaw += differenceToRealBodyYaw * 0.3F;

        float differenceToHeadYaw = toMinecraftDegrees(nmsParrot.yHeadRot - bodyYaw);

        if (Math.abs(differenceToHeadYaw) > MAX_YAW_DIFF_BETWEEN_HEAD_AND_BODY) {
            this.bodyYaw += differenceToHeadYaw - Math.signum(differenceToHeadYaw) * MAX_YAW_DIFF_BETWEEN_HEAD_AND_BODY;
        }

        this.bodyYaw = toMinecraftDegrees(bodyYaw);
    }
}
