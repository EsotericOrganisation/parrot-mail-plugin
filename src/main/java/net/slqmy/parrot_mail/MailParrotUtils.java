package net.slqmy.parrot_mail;

import io.papermc.paper.math.Rotations;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Parrot;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class MailParrotUtils {
    public static void updateBundlePosition(@NotNull Parrot parrot, @NotNull ArmorStand armorStand) {
        Vector facingDirection = parrot.getLocation().getDirection();
        facingDirection.normalize();

        Vector facingDirectionLeft = facingDirection.clone();
        facingDirectionLeft.rotateAroundY(Math.PI / 2.0D);
        facingDirectionLeft.normalize();

        Vector facingDirectionUp = facingDirection.clone();
        facingDirectionUp.crossProduct(facingDirectionLeft);
        facingDirectionUp.normalize();

        Bukkit.getLogger().info("Up direction: " + facingDirectionUp);

        Bukkit.getLogger().info("Up & forward: " + facingDirectionUp.angle(facingDirection) * 180 / Math.PI);
        Bukkit.getLogger().info("Up & left: " + facingDirectionUp.angle(facingDirectionLeft) * 180 / Math.PI);
        Bukkit.getLogger().info("Forward & left: " + facingDirection.angle(facingDirectionLeft) * 180 / Math.PI);

        double yOffset = parrot.isSitting() ? 1D : 0.86D;

        Location armorStandLocation = parrot.getLocation().add(facingDirection.multiply(0.6D)).add(facingDirectionUp.multiply(-yOffset));

        armorStand.teleport(armorStandLocation);
        // armorStand.setHeadRotations(Rotations.ofDegrees(parrot.getYaw() - 10, 0, -3));
    }
}
