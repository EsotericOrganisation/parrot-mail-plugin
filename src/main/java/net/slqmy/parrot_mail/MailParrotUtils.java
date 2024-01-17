package net.slqmy.parrot_mail;

import io.papermc.paper.math.Rotations;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Parrot;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.logging.Level;

public class MailParrotUtils {
    public static void updateBundlePosition(@NotNull Parrot parrot, @NotNull ItemDisplay bundle) {
        Location parrotLocation = parrot.getLocation();
        Location bundleLocation = bundle.getLocation();

        Vector forward = parrotLocation.getDirection();
        Vector left = forward.clone().rotateAroundY(Math.PI / 2);
        Vector up = forward.clone().crossProduct(left);

        Transformation transformation = bundle.getTransformation();

        Vector3f parrotTranslation = new Vector3f(
                (float) (parrotLocation.getX() - bundleLocation.getX()),
                (float) (parrotLocation.getY() - bundleLocation.getY()),
                (float) (parrotLocation.getZ() - bundleLocation.getZ())
        );

        parrotTranslation.add(new Vector3f((float) up.getX(), (float) up.getY(), (float) up.getZ()).mul(0.4F));
        parrotTranslation.add(new Vector3f((float) forward.getX(), (float) forward.getY(), (float) forward.getZ()).mul(0.3F));

        transformation.getTranslation().set(parrotTranslation);

        bundle.setTransformation(transformation);
    }
}
