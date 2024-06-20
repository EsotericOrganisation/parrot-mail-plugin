package net.slqmy.parrot_mail.event;

import net.kyori.adventure.text.Component;
import net.slqmy.parrot_mail.ParrotMailPlugin;
import net.slqmy.parrot_mail.parrot.MailParrot;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BundleMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class ParrotRightClickListener implements Listener {
    private final ParrotMailPlugin plugin;

    public ParrotRightClickListener() {
        this.plugin = ParrotMailPlugin.getInstance();
    }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onParrotRightClick(@NotNull PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Bukkit.broadcast(Component.text("player interact entity"));

        // Minecraft is dumb and sends packets for both main and off hand interactions
//        if (event.getRightClicked() instanceof Parrot && event.getHand() == EquipmentSlot.OFF_HAND) {
//            if (event.getPlayer().getInventory().getItemInOffHand().isEmpty()) {
//                event.setCancelled(true);
//            }
//        }

        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        if (!player.isSneaking()) {
            return;
        }

        if (!(event.getRightClicked() instanceof Parrot parrot)) {
            return;
        }

        if (!parrot.isTamed()) {
            return;
        }

        MailParrot mailParrot = Objects.requireNonNullElseGet(
            MailParrot.from(parrot), () -> new MailParrot(parrot));

        PlayerInventory playerInventory = player.getInventory();
        ItemStack heldItem = playerInventory.getItemInMainHand();

        if (mailParrot.hasGuideItem()) {
            mailParrot.removeGuideItem(player);
            mailParrot.removeIfInactive();

            event.setCancelled(true);
            return;
        }

        if (mailParrot.hasBundle()) {
            handleUsedItem(player, mailParrot, heldItem);
            mailParrot.removeIfInactive();

            event.setCancelled(true);
            return;
        }

        if (heldItem.getType() == Material.BUNDLE) {
            BundleMeta bundleMeta = (BundleMeta) heldItem.getItemMeta();
            boolean hasItems = !bundleMeta.getItems().isEmpty();

            if (hasItems) {
                mailParrot.giveBundle(player, heldItem);

                event.setCancelled(true);
                return;
            }
        }

        // No interaction with the parrot
        mailParrot.removeIfInactive();
    }

    private void handleUsedItem(Player player, MailParrot parrot, ItemStack usedItem) {
        switch (usedItem.getType()) {
            case COMPASS -> {
                parrot.setGuideItem(player, usedItem);
                parrot.readGuideItemAndFlyToTarget();
            }
            case MAP -> {

            }
            case NAME_TAG -> {

            }
            default -> {
                Bukkit.broadcast(Component.text("Bundle has been taken from parrot"));
                parrot.removeBundle(player);
            }
        }
    }
}
