package net.slqmy.parrot_mail.event;

import io.papermc.paper.event.entity.EntityMoveEvent;
import net.slqmy.parrot_mail.ParrotMailPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Parrot;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ParrotMoveEvent implements Listener {

    private final ParrotMailPlugin plugin;

    public ParrotMoveEvent(ParrotMailPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onParrotMove(@NotNull EntityMoveEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof Parrot parrot) {
            PersistentDataContainer container = parrot.getPersistentDataContainer();

            NamespacedKey key = new NamespacedKey(plugin, "bundle_display");

            String UuidString = container.get(key, PersistentDataType.STRING);

            if (UuidString == null) {
                return;
            }

            UUID uuid = UUID.fromString(UuidString);

            World world = parrot.getWorld();

            ItemDisplay itemDisplay = (ItemDisplay) world.getEntity(uuid);

            itemDisplay.teleport(parrot.getLocation().add(0, .3, 0).add(parrot.getLocation().getDirection().multiply(.3)));
        }
    }
}
