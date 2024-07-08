package net.slqmy.parrot_mail.parrot;

import lombok.Getter;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.player.Player;
import net.slqmy.parrot_mail.ParrotMailPlugin;
import net.slqmy.parrot_mail.util.MathUtils;
import net.slqmy.parrot_mail.util.ParrotRotation;
import net.slqmy.parrot_mail.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Parrot;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static net.kyori.adventure.text.Component.text;
import static net.slqmy.parrot_mail.util.MathUtils.toMinecraftDegrees;

public final class BundlePositioner implements Runnable {
    @Getter
    private static final List<BundlePositioner> activePositioners = new ArrayList<>();

    private final Parrot parrot;
    private final net.minecraft.world.entity.animal.Parrot nmsParrot;
    private final ParrotRotation parrotRotation;

    private final ItemDisplay bundle;
    private final net.minecraft.world.entity.Display.ItemDisplay nmsBundle;

    private float animHeadYaw;
    private float animBodyYaw;

    private float angle;

    private int lastLookAtCooldown;
    private int spinning;

    private boolean isMovingO;
    private boolean isRotatingHeadO;
    private boolean isRotatingBodyO;

    private boolean cancelled;
    private boolean firstTick;

    BundlePositioner(Parrot parrot, ItemDisplay bundle) {
        this.parrot = parrot;
        this.nmsParrot = Utils.toNMS(parrot);
        this.parrotRotation = new ParrotRotation(parrot);

        this.bundle = bundle;
        this.nmsBundle = Utils.toNMS(bundle);

        this.animHeadYaw = parrot.getYaw();
        this.animBodyYaw = parrot.getBodyYaw();

        this.cancelled = false;
        this.firstTick = true;

        this.lastLookAtCooldown = 0;

        activePositioners.add(this);
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void cancel() {
        this.cancelled = true;
    }

    public void runTickStartTasks() {
        if (!parrot.isValid()) {
            return;
        }

        this.isMovingO = isMoving();
        this.isRotatingHeadO = isRotatingHead();
        this.isRotatingBodyO = isRotatingBody();
    }

    @Override
    public void run() {
        if (!parrot.isValid()) {
            return;
        }

        if (checkSpinningLikeCrazy()) {
            this.spinning = 3;
        } else if (spinning > 0) {
            this.spinning--;
        }

        parrotRotation.tick();

        this.animHeadYaw = toMinecraftDegrees(calculateAnimHeadYaw());
        this.animBodyYaw = toMinecraftDegrees(calculateAnimBodyYaw());

        if (ParrotMailPlugin.getInstance().isDebugging()) {
            Bukkit.broadcast(text("anim head: " + animHeadYaw));
            Bukkit.broadcast(text("anim body " + animBodyYaw));
        }

        //Remove pitch from the equation cuz f*ck this sh*t
        nmsParrot.setXRot(0.0F);
        nmsParrot.xRotO = 0.0F;

        Transformation transformation = bundle.getTransformation();

        //Find the bundle location
        Vector3f difference = getBundlePositionDelta();
        transformation.getTranslation().set(difference);

        //Rotate the bundle transformation
        float yaw = (float) Math.toRadians(MathUtils.toNormalDegrees(animHeadYaw - angle));
        AxisAngle4f rotation = new AxisAngle4f(yaw, -0.05F, 1.0F, -0.04F);

        transformation.getLeftRotation().set(rotation);

        //Rotate the bundle facing direction
        nmsBundle.setYRot(angle);

        //Apply the transformation
        bundle.setTransformation(transformation);

        bundle.setInterpolationDelay(getInterpolationDelay());
        bundle.setInterpolationDuration(getInterpolationDuration());
        bundle.setTeleportDuration(getTeleportDuration());

        //Broadcast packets
        broadcastBundleDataUpdatePackets();

        if (firstTick) {
            this.firstTick = false;
        }
    }

    private Vector3f getBundlePositionDelta() {
        Location parrotEyeLocation = parrot.getEyeLocation();

        Vector bodyDirection = MathUtils.getFacingDirection(animBodyYaw, 0.0F);
        Location atNeck = parrotEyeLocation.clone().add(bodyDirection.normalize().multiply(0.161D));

        Vector headDirection = MathUtils.getFacingDirection(animHeadYaw, 0.0F);
        Location atBeak = atNeck.clone().add(headDirection.normalize().multiply(0.151D));

        Vector right = MathUtils.getFacingDirection(90.0F, 0.0F);

        double dx = atBeak.getX() - parrotEyeLocation.getX();
        double dz = atBeak.getZ() - parrotEyeLocation.getZ();

        float yOffset = parrot.isSitting() ? -0.185F : -0.055F;
        float length = (float) Math.sqrt(dx * dx + dz * dz);

        this.angle = toMinecraftDegrees((float) (Math.atan2(dz, dx) * MathUtils.RADIANS_TO_DEGREES - 90.0F));

        return new Vector3f(0.0F, 0.0F,1.0F)
            .mul(length)
            .add(right.toVector3f().normalize().mul(0.025F))
            .add(0.0F, yOffset, 0.0F);
    }

    private float calculateAnimHeadYaw() {
        if (isSpinningLikeCrazy()) {
            return MathUtils.lerpRotation(animHeadYaw, nmsParrot.yHeadRotO, (1.0F / 3.0F));
        }

        if (isRotationStable()) {
            return nmsParrot.yHeadRot;
        }

        if (!isRotatingHeadO) {
            return animHeadYaw;
        }

        return nmsParrot.yHeadRotO;
    }

    private float calculateAnimBodyYaw() {
        if (isSpinningLikeCrazy()) {
            return MathUtils.lerpRotation(animBodyYaw, nmsParrot.yBodyRotO, (1.0F / 3.0F));
        }

        if (isRotationStable()) {
            parrotRotation.setVisualBodyYaw(nmsParrot.yBodyRot);
            return nmsParrot.yBodyRot;
        }

        if (!isRotatingBody()) {
            return animBodyYaw;
        }

        if (!isMovingO) {
            float rotatingDirection = Math.signum(MathUtils.toMinecraftDegrees(nmsParrot.yBodyRot - nmsParrot.yBodyRotO));
            float prediction = rotatingDirection == 0.0F
                ? nmsParrot.yBodyRot
                : animBodyYaw + (rotatingDirection * 7.5F);

            return clampBodyRotToHead(toMinecraftDegrees(prediction));
        }

        return parrotRotation.getVisualBodyYaw();
    }

    private void broadcastBundleDataUpdatePackets() {
        List<SynchedEntityData.DataValue<?>> entityData = nmsBundle.getEntityData().packAll();
        assert entityData != null;

        for (Player player : nmsBundle.level().players()) {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            serverPlayer.connection.send(new ClientboundSetEntityDataPacket(
                nmsBundle.getId(), entityData));
        }
    }

    /**
     * Clamps the body rotation to the head rotation. The body can't fall behind by more than 75 degrees, and can't be ahead.
     * @param bodyYaw The body yaw.
     * @return The clamped body yaw.
     */
    private float clampBodyRotToHead(float bodyYaw) {
        float clamped = bodyYaw;
        float bodyRotatingDirection = Math.signum(toMinecraftDegrees(nmsParrot.yBodyRot - nmsParrot.yBodyRotO));

        float difference = toMinecraftDegrees(animHeadYaw - bodyYaw);

        if (Math.abs(difference) > 75.0F) {
            clamped = animHeadYaw - (Math.signum(difference) * 75.0F);
        }

        if (bodyRotatingDirection == 0.0F) {
            return clamped;
        }

        return MathUtils.limitRot(clamped, animHeadYaw, bodyRotatingDirection);
    }

    private boolean isLookingAtEntity() {
        int lastLookCooldown = lastLookAtCooldown;
        int currentLookCooldown = Utils.reflectField(LookControl.class, "lookAtCooldown", nmsParrot.getLookControl());

        this.lastLookAtCooldown = currentLookCooldown;
        return currentLookCooldown != 0 || lastLookCooldown != 0;
    }

    private boolean hasReachedWantedRotation() {
        Optional<Float> yRotD = Utils.invokeReflectedMethod(LookControl.class, "getYRotD", nmsParrot.getLookControl());
        return yRotD.isEmpty();
    }

    private boolean isMoving() {
        double d = nmsParrot.getX() - nmsParrot.xo;
        double e = nmsParrot.getZ() - nmsParrot.zo;
        return d * d + e * e > 2.5000003E-7F;
    }

    private boolean isRotatingHead() {
        boolean isRotatingHead = toMinecraftDegrees(nmsParrot.yHeadRot) != toMinecraftDegrees(nmsParrot.yHeadRotO);
        boolean isAnimHeadRotCatchingUp = toMinecraftDegrees(animHeadYaw) != toMinecraftDegrees(nmsParrot.yHeadRot);

        return isRotatingHead || isAnimHeadRotCatchingUp;
    }

    private boolean isRotatingBody() {
        boolean isRotatingBody = toMinecraftDegrees(nmsParrot.yBodyRot) != toMinecraftDegrees(nmsParrot.yBodyRotO);
        boolean isAnimBodyRotCatchingUp = toMinecraftDegrees(animBodyYaw) != toMinecraftDegrees(nmsParrot.yBodyRot);

        return isRotatingBody || isAnimBodyRotCatchingUp;
    }

    private boolean checkSpinningLikeCrazy() {
        return
            isMoving() &&
            isRotatingHead() &&
            isRotatingBody() &&
            Math.abs(nmsParrot.yBodyRot - nmsParrot.yHeadRot) > 74.75F &&
            Math.abs(nmsParrot.yBodyRot - nmsParrot.yHeadRot) < 75.25F;
    }

    private boolean isSpinningLikeCrazy() {
        return spinning > 0;
    }

    private boolean isRotationStable() {
        return
            !isSpinningLikeCrazy() &&
            !isRotatingHead() &&
            !isRotatingBody() &&
            !isRotatingHeadO &&
            !isRotatingBodyO;
    }

    private int getInterpolationDelay() {
        if (firstTick) {
            return 0;
        }

        return (!isRotatingBodyO && isRotatingBody()) ? 1 : 0;
    }

    private int getInterpolationDuration() {
        if (firstTick || isSpinningLikeCrazy()) {
            return 0;
        }

        return isRotatingHead() ? 1 : 0;
    }

    private int getTeleportDuration() {
        if (firstTick || isSpinningLikeCrazy()) {
            return 0;
        }

        return isRotatingBody() ? 1 : 0;
    }
}
