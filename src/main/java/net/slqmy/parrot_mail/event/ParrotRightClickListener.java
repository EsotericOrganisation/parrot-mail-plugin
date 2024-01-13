package net.slqmy.parrot_mail.event;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

public class ParrotRightClickListener implements Listener {
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

            Player player = event.getPlayer();

            EntityEquipment parrotEquipment = parrot.getEquipment();

            ItemStack possibleBundle = parrotEquipment.getItemInMainHand();

            if (!possibleBundle.isEmpty()) {
                parrotEquipment.setItemInMainHand(null);

                Location parrotLocation = parrot.getLocation();

                parrotLocation.getWorld().dropItemNaturally(parrotLocation, possibleBundle);
                return;
            }

            PlayerInventory playerInventory = player.getInventory();
            ItemStack heldItem = playerInventory.getItemInMainHand();

            if (heldItem.getType() != Material.BUNDLE) {
                return;
            }

            playerInventory.setItemInMainHand(null);
            parrotEquipment.setItemInMainHand(heldItem);
        }
    }
}
