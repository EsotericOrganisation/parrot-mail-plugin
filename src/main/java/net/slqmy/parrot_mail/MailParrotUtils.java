package net.slqmy.parrot_mail;

import io.papermc.paper.math.Rotations;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Parrot;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class MailParrotUtils {
    public static void updateBundlePosition(@NotNull Parrot parrot, @NotNull ItemDisplay bundle) {
        bundle.teleport(parrot.getLocation().add(0, 1, 0));
    }
}
