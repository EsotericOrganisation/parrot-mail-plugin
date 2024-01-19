package net.slqmy.parrot_mail;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.joml.AxisAngle4d;
import org.joml.Vector3d;
import org.joml.Vector3f;

public class MailParrotUtils {
    public static @NotNull Location updateBundlePosition(@NotNull Parrot parrot, @NotNull ItemDisplay bundle, Location lastLocation, boolean shouldTP) {
        Location parrotLocation = parrot.getLocation();
        Location bundleLocation = bundle.getLocation();

        if (shouldTP) {
            bundle.teleport(parrotLocation);
            bundle.setRotation(0, 0);
        }

        Vector forward = parrotLocation.getDirection();
        Vector left = forward.clone().rotateAroundY(Math.PI / 2);
        Vector up = forward.clone().crossProduct(left);

        Transformation transformation = bundle.getTransformation();

        double yawRadians = -Math.toRadians(parrot.getYaw());

        transformation.getLeftRotation().set(new AxisAngle4d(yawRadians, new Vector3d(0, 1, 0)));

        Vector3f parrotTranslation = new Vector3f(
                (float) (parrotLocation.getX() - bundleLocation.getX()),
                (float) (parrotLocation.getY() - bundleLocation.getY()),
                (float) (parrotLocation.getZ() - bundleLocation.getZ())
        );

        parrotTranslation.add(new Vector3f((float) up.getX(), (float) up.getY(), (float) up.getZ()).mul(0.4F));
        parrotTranslation.add(new Vector3f((float) forward.getX(), (float) forward.getY(), (float) forward.getZ()).mul(0.3F));

        bundle.setInterpolationDelay(0);
        bundle.setInterpolationDuration(lastLocation.equals(parrotLocation) || shouldTP ? 0 : 1);

        transformation.getTranslation().set(parrotTranslation);
        bundle.setTransformation(transformation);

        return parrotLocation;
    }

    public static void updateMailParrot(@NotNull Parrot parrot) {
        parrot.setRotation(parrot.getYaw(), 0);
    }

    public static boolean hasBundle(@NotNull Parrot parrot) {
        return parrot.getEquipment().getItemInMainHand().getType() == Material.BUNDLE;
    }

    public static void removeBundleFromParrot(@NotNull Parrot parrot, @NotNull Player player) {
        PlayerInventory playerInventory = player.getInventory();
        ItemStack heldItem = playerInventory.getItemInMainHand();

        EntityEquipment parrotEquipment = parrot.getEquipment();

        ItemStack bundle = parrotEquipment.getItemInMainHand();
        parrotEquipment.setItemInMainHand(null);

        Location parrotLocation = parrot.getLocation();

        if (heldItem.isEmpty()) {
            playerInventory.setItemInMainHand(bundle); // Gives the bundle directly to the player.
        } else {
            parrotLocation.getWorld().dropItem(parrotLocation, bundle); // Drops the parrot's bundle.
        }
    }

    public static void giveParrotBundle(@NotNull Parrot parrot, @NotNull Player player) {
        EntityEquipment parrotEquipment = parrot.getEquipment();
        PlayerInventory playerInventory = player.getInventory();
        ItemStack bundle = playerInventory.getItemInMainHand();

        playerInventory.setItemInMainHand(null);
        parrotEquipment.setItemInMainHand(bundle);
        parrotEquipment.setDropChance(EquipmentSlot.HAND, 1.0F);
    }
}
