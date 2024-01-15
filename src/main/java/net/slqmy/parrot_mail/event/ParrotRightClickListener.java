package net.slqmy.parrot_mail.event;

import io.papermc.paper.math.Rotations;
import net.slqmy.parrot_mail.MailParrotUtils;
import net.slqmy.parrot_mail.ParrotMailPlugin;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.BundleMeta;
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


            EntityEquipment parrotEquipment = parrot.getEquipment();

            ItemStack possibleBundle = parrotEquipment.getItemInMainHand();

            World world = parrot.getWorld();

            PlayerInventory playerInventory = player.getInventory();
            ItemStack heldItem = playerInventory.getItemInMainHand();

            // ItemStack.isEmpty() checks if an item exists or not.
            if (possibleBundle.getType() == Material.BUNDLE) { // If the parrot has a bundle.
                parrotEquipment.setItemInMainHand(null);

                Location parrotLocation = parrot.getLocation();

                if (heldItem.isEmpty()) {
                    playerInventory.setItemInMainHand(possibleBundle); // Gives the bundle directly to the player.
                } else {
                    world.dropItem(parrotLocation, possibleBundle); // Drops the parrot's bundle.
                }

                event.setCancelled(true);
                return;
            }


            if (heldItem.getType() != Material.BUNDLE) {
                return;
            }

            playerInventory.setItemInMainHand(null);
            parrotEquipment.setItemInMainHand(heldItem);

            event.setCancelled(true);

            parrotEquipment.setDropChance(EquipmentSlot.HAND, 1.0F);

            // Spawn armour stand

            Vector facingDirection = parrot.getLocation().getDirection();

            Vector facingDirectionDown = facingDirection.clone();
            Vector facingDirectionSide = facingDirectionDown.clone();

            facingDirectionDown.rotateAroundZ(Math.PI / 2);
            facingDirectionSide.rotateAroundY(Math.PI / 2);

            Location armorStandLocation = parrot.getLocation().add(facingDirection.multiply(0.6)).add(facingDirectionDown.multiply(0.87)).add(facingDirectionSide.multiply(-0.01));

            ArmorStand armorStand = (ArmorStand) world.spawnEntity(armorStandLocation, EntityType.ARMOR_STAND);

            armorStand.setSmall(true);
            armorStand.setInvulnerable(true);
            armorStand.setVisible(false);
            armorStand.setHeadRotations(Rotations.ofDegrees(-10, 0, -3));

            ItemStack bundle = new ItemStack(Material.BUNDLE);
            BundleMeta meta = (BundleMeta) bundle.getItemMeta();

            meta.addItem(new ItemStack(Material.DIRT));

            bundle.setItemMeta(meta);

            armorStand.getEquipment().setHelmet(bundle);

            Bukkit.getScheduler().runTaskTimer(plugin, () -> MailParrotUtils.updateBundlePosition(parrot, armorStand), 0, 1);
        }
    }
}
