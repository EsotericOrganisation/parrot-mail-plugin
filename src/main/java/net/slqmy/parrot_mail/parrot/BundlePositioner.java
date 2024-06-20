package net.slqmy.parrot_mail.parrot;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.player.Player;
import net.slqmy.parrot_mail.ParrotMailPlugin;
import net.slqmy.parrot_mail.util.MathUtils;
import net.slqmy.parrot_mail.util.ParrotRotation;
import net.slqmy.parrot_mail.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftDisplay;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Parrot;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

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

    private boolean isMoving;
    private boolean isRotatingHead;
    private boolean isRotatingBody;

    private boolean cancelled;

    BundlePositioner(Parrot parrot, ItemDisplay bundle) {
        this.parrot = parrot;
        this.nmsParrot = Utils.toNMS(parrot);
        this.parrotRotation = new ParrotRotation(parrot);

        this.bundle = bundle;
        this.nmsBundle = Utils.toNMS(bundle);

        this.animHeadYaw = parrot.getYaw();
        this.animBodyYaw = parrot.getBodyYaw();

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

        this.isMoving = isMoving();
        this.isRotatingHead = isRotatingHead();
        this.isRotatingBody = isRotatingBody();
    }

    @Override
    public void run() {
        if (!parrot.isValid()) {
            return;
        }

        parrotRotation.tick();

        this.animHeadYaw = calculateAnimHeadYaw();
        this.animBodyYaw = animBodyYaw();

        //Remove pitch from the equation cuz f*ck this sh*t
        nmsParrot.setXRot(0.0F);
        nmsParrot.xRotO = 0.0F;

        Transformation transformation = bundle.getTransformation();

        //Find the bundle location
        Vector3f difference = getBundlePositionDelta();
        transformation.getTranslation().set(difference);

        //Rotate the bundle transformation
        float yaw = (float) Math.toRadians(MathUtils.mcYawToAngle(animHeadYaw - angle));
        AxisAngle4f rotation = new AxisAngle4f(yaw, 0.0F, 1.0F, 0.0F);

        transformation.getLeftRotation().set(rotation);

        //Rotate the bundle facing direction
        nmsBundle.setYRot(angle);

        //Apply the transformation
        bundle.setTransformation(transformation);

        bundle.setInterpolationDelay(0);
        bundle.setInterpolationDuration((isRotatingHead || isRotatingBody) ? 1 : 0);
        bundle.setTeleportDuration(isRotatingBody ? 1 : 0);

        //Broadcast packets
        broadcastUpdatePackets();
    }

    private Vector3f getBundlePositionDelta() {
        Location parrotEyeLocation = parrot.getEyeLocation();

        Vector bodyDirection = MathUtils.getFacingDirection(animBodyYaw, 0.0F);
        Location atNeck = parrotEyeLocation.clone().add(bodyDirection.normalize().multiply(0.161));

        Vector headDirection = MathUtils.getFacingDirection(animHeadYaw, 0.0F);
        Location atBeak = atNeck.clone().add(headDirection.normalize().multiply(0.158));

        Vector rightFromHead = MathUtils.getFacingDirection(animHeadYaw + 90.0F, 0.0F);
        Location atRight = atBeak.clone().add(rightFromHead.normalize().multiply(0.035));

        double dx = atRight.getX() - parrotEyeLocation.getX();
        double dz = atRight.getZ() - parrotEyeLocation.getZ();

        float yOffset = parrot.isSitting() ? -0.125F : 0.05F;
        float length = (float) Math.sqrt(dx * dx + dz * dz);

        this.angle = MathUtils.toMinecraftDegrees((float) Math.atan2(dz, dx) * MathUtils.RADIANS_TO_DEGREES);
        return new Vector3f(1.0F, 0.0F,0.0F).mul(length).add(0.0F, yOffset, 0.0F);
    }

    private float calculateAnimHeadYaw() {
        float angle = isRotatingHead
            ? MathUtils.lerp(nmsParrot.yHeadRotO, nmsParrot.yHeadRot, 0.75F)
            : nmsParrot.yHeadRot;

        if (Math.abs(angle - animHeadYaw) > nmsParrot.getHeadRotSpeed()) {
            angle = animHeadYaw + Math.signum(angle - animHeadYaw) * 10;
        }

        return angle;
    }

    private float animBodyYaw() {
//        BodyRotationControl bodyRotationControl = Utils.reflectField(Mob.class, "bodyRotationControl", nmsParrot);
//
//        float lastStableYHeadRot = Utils.reflectField(BodyRotationControl.class, "lastStableYHeadRot", bodyRotationControl);
//        int headStableTime = Utils.reflectField(BodyRotationControl.class, "headStableTime", bodyRotationControl);
//
//        if (Math.abs(animHeadYaw - lastStableYHeadRot) > 15.0F) {
//            return Mth.rotateIfNecessary(nmsParrot.yBodyRot, nmsParrot.yHeadRot, (float) nmsParrot.getMaxHeadYRot());
//        } else {
//            if (++headStableTime > 10) {
//                float interpolationFactor = Mth.clamp((headStableTime - 10) / 10.0F, 0.0F, 1.0F);
//                float maxRot = (float) nmsParrot.getMaxHeadYRot() * (1.0F - interpolationFactor);
//
//                return Mth.rotateIfNecessary(nmsParrot.yBodyRot, nmsParrot.yHeadRot, maxRot);
//            }
//        }

//        if (isMoving() && isRotating()) {
//            int totalLerpSteps = (int) Math.ceil(Math.abs(nmsParrot.yBodyRot - nmsParrot.yBodyRotO) / 10.0F);
//            int remainingLerpSteps = (int) Math.ceil(Math.abs(nmsParrot.yBodyRot - animBodyYaw) / 10.0F);
//
//            // begin interpolation immediately
//            if (remainingLerpSteps == totalLerpSteps) {
//                remainingLerpSteps -= 1;
//            }
//
//            return
//                (nmsParrot.yBodyRotO) +
//                (nmsParrot.yBodyRot - nmsParrot.yBodyRotO) *
//                (1.0F - (remainingLerpSteps / (float) totalLerpSteps));
//        } else {
//            return nmsParrot.yBodyRot;
//        }

        if (!isRotatingBody) {
            return nmsParrot.yBodyRot;
        }

        if (!isMoving) {
            if (ParrotMailPlugin.getInstance().isDebugging()) {
                Bukkit.broadcast(Component.text("Not moving, and rotating"));
            }

            float factor = 1.2F;
            return MathUtils.lerp(nmsParrot.yBodyRotO, nmsParrot.yBodyRot, factor);
        }

        if (ParrotMailPlugin.getInstance().isDebugging()) {
            Bukkit.broadcast(MiniMessage.miniMessage().deserialize("<green>Using parrot rotation manager"));
        }

        return parrotRotation.getVisualBodyYaw();
    }

    private void broadcastUpdatePackets() {
        Display nmsBundle = ((CraftDisplay) bundle).getHandle();
        List<SynchedEntityData.DataValue<?>> entityData = nmsBundle.getEntityData().packAll();

        assert entityData != null;
        for (Player player : nmsBundle.level().players()) {
            ((ServerPlayer) player).connection.send(new ClientboundSetEntityDataPacket(
                nmsBundle.getId(), entityData));
        }
    }

//    private int getInterpolationDelay() {
//        return (int) Math.ceil((Math.abs(nmsParrot.yHeadRot - nmsParrot.yHeadRotO) / 10.0F));
//    }

//    private float calculateAnimBodyYaw() {
//        boolean debug = ParrotMailPlugin.getInstance().isDebugging();
//        boolean bodyYawChanged = parrotRotation.tickMovement();
//
//        if (isLookingAtEntity()) {
//            if (isMoving()) {
//                //pretty good but sometimes slightly off for extended periods of time
//                //sometimes flashes
//                if (debug && lastLookType != 1) {
//                    this.lastLookType = 1;
//                    Bukkit.broadcast(Component.text("Moving and looking at target"));
//                }
//
//                //return nmsParrot.getYRot();
//                return bodyYawChanged
//                    ? parrotRotation.getVisualBodyYaw()
//                    : Mth.rotateIfNecessary(nmsParrot.yBodyRot, nmsParrot.yHeadRot, nmsParrot.getMaxHeadYRot());
//            } else {
//                //slightly off when rotating body, bundle is a slight bit behind
//                if (debug && lastLookType != 2) {
//                    this.lastLookType = 2;
//                    Bukkit.broadcast(Component.text("Not moving, and looking at target"));
//                }
//
//                //testing clienttick
//                return clientTick();
//            }
//        } else {
//            if (isMoving()) {
//                //flawless
//                if (debug && lastLookType != 3) {
//                    this.lastLookType = 3;
//                    Bukkit.broadcast(Component.text("Moving, not looking at target"));
//                }
//
//                return bodyYawChanged
//                    ? parrotRotation.getVisualBodyYaw()
//                    : Mth.rotateIfNecessary(nmsParrot.yBodyRot, nmsParrot.yHeadRot, nmsParrot.getMaxHeadYRot());
//            } else {
//                //resolved, this works. nice
//                if (debug && lastLookType != 4) {
//                    this.lastLookType = 4;
//                    Bukkit.broadcast(Component.text("Not moving, not looking at target"));
//                }
//
//                return animHeadYaw;
//            }
//        }
//    }
//
//    private float clientTick() {
//        BodyRotationControl bodyRotationControl = Utils.reflectField(Mob.class, "bodyRotationControl", nmsParrot);
//
//        float lastStableYHeadRot = Utils.reflectField(BodyRotationControl.class, "lastStableYHeadRot", bodyRotationControl);
//        int headStableTime = Utils.reflectField(BodyRotationControl.class, "headStableTime", bodyRotationControl);
//
//        if (Math.abs(animHeadYaw - lastStableYHeadRot) > 15.0F) {
//            return Mth.rotateIfNecessary(animBodyYaw, animHeadYaw, (float) nmsParrot.getMaxHeadYRot());
//        } else {
//            if (++headStableTime > 10) {
//                float interpolationFactor = Mth.clamp((headStableTime - 10) / 10.0F, 0.0F, 1.0F);
//                float maxRot = (float) nmsParrot.getMaxHeadYRot() * (1.0F - interpolationFactor);
//
//                return Mth.rotateIfNecessary(animBodyYaw, animHeadYaw, maxRot);
//            }
//        }
//
//        return animBodyYaw;
//    }

    private boolean isLookingAtEntity() {
        int lastLookCooldown = lastLookAtCooldown;
        int currentLookCooldown = Utils.reflectField(LookControl.class, "lookAtCooldown", nmsParrot.getLookControl());

        this.lastLookAtCooldown = currentLookCooldown;
        return currentLookCooldown != 0 || lastLookCooldown != 0;
    }

    private boolean isMoving() {
        double d = nmsParrot.getX() - nmsParrot.xo;
        double e = nmsParrot.getZ() - nmsParrot.zo;
        return d * d + e * e > 2.5000003E-7F;
    }

    private boolean isRotatingHead() {
        boolean isRotatingHead = nmsParrot.yHeadRot != nmsParrot.yHeadRotO;
        boolean isAnimHeadRotCatchingUp = animHeadYaw != nmsParrot.yHeadRot;

        return isRotatingHead || isAnimHeadRotCatchingUp;
    }

    private boolean isRotatingBody() {
        boolean isRotatingBody = nmsParrot.yBodyRot != nmsParrot.yBodyRotO;
        boolean isAnimBodyRotCatchingUp = animBodyYaw != nmsParrot.yBodyRot;

        return isRotatingBody || isAnimBodyRotCatchingUp;
    }
}
