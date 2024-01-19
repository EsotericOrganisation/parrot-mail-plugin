package net.slqmy.parrot_mail.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.slqmy.parrot_mail.MailParrotUtils;
import net.slqmy.parrot_mail.ParrotMailPlugin;
import net.slqmy.parrot_mail.runnables.MailParrotUpdater;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class ParrotRightClickListener implements Listener {

    private final ParrotMailPlugin plugin;

    public ParrotRightClickListener(ParrotMailPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onParrotRightClick(@NotNull PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();

        if (!player.isSneaking()) {
            return;
        }

        Entity entity = event.getRightClicked();

        if (entity instanceof Parrot parrot) {
            if (!parrot.isTamed()) {
                return;
            }

            PlayerInventory playerInventory = player.getInventory();
            ItemStack heldItem = playerInventory.getItemInMainHand();

            if (MailParrotUtils.hasBundle(parrot)) {
                switch (heldItem.getType()) {
                    case MAP:
                        event.setCancelled(true);
                        return;
                    case COMPASS:
                        event.setCancelled(true);
                        return;
                    case NAME_TAG:
                        boolean success = handleNameTag(heldItem, parrot);

                        if (success) {
                            event.setCancelled(true);
                        }

                        return;
                    default:
                        event.setCancelled(true);
                        MailParrotUtils.removeBundleFromParrot(parrot, player);
                        return;
                }
            }

            if (heldItem.getType() != Material.BUNDLE) {
                return;
            }

            MailParrotUtils.giveParrotBundle(parrot, player);
            event.setCancelled(true);

            ItemDisplay itemDisplay = spawnItemDisplay(parrot.getLocation());

            new MailParrotUpdater(parrot, itemDisplay).runTaskTimer(plugin, 0, 1);
        }
    }

    private boolean handleNameTag(@NotNull ItemStack nameTag, Parrot parrot) {
        ItemMeta nameTagMeta = nameTag.getItemMeta();

        if (!nameTagMeta.hasDisplayName()) {
            return false;
        }

        Component playerName = nameTagMeta.displayName();
        assert playerName != null;

        PlainTextComponentSerializer serializer = PlainTextComponentSerializer.plainText();

        String playerNameString = serializer.serialize(playerName);
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayerIfCached(playerNameString);

        if (offlinePlayer == null) {
            return false;
        }

        MailParrotUtils.sendParrot(parrot, offlinePlayer);
        return true;
    }

    private @NotNull ItemDisplay spawnItemDisplay(@NotNull Location spawnLocation) {
        World world = spawnLocation.getWorld();

        spawnLocation.setDirection(new Vector());
        spawnLocation.setYaw(0);
        spawnLocation.setPitch(0);

        ItemDisplay itemDisplay = (ItemDisplay) world.spawnEntity(spawnLocation, EntityType.ITEM_DISPLAY);

        ItemStack bundle = new ItemStack(Material.BUNDLE);

        BundleMeta meta = (BundleMeta) bundle.getItemMeta();
        meta.addItem(new ItemStack(Material.DIRT));

        bundle.setItemMeta(meta);

        itemDisplay.setItemStack(bundle);

        itemDisplay.setInterpolationDelay(0);
        itemDisplay.setInterpolationDuration(1);

        itemDisplay.setBillboard(Display.Billboard.FIXED);

        Transformation transformation = itemDisplay.getTransformation();
        transformation.getScale().set(0.5D, 0.5D, 0.5D);
        itemDisplay.setTransformation(transformation);

        return itemDisplay;
    }
}
