package net.slqmy.parrot_mail;

import org.bukkit.Location;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Parrot;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class MailParrotUtils {
    public static Location updateBundlePosition(@NotNull Parrot parrot, @NotNull ItemDisplay bundle, Location lastLocation, boolean shouldTP) {
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
}
