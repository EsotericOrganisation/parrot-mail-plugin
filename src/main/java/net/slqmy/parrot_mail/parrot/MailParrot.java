package net.slqmy.parrot_mail.parrot;

import lombok.Getter;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.LandOnOwnersShoulderGoal;
import net.slqmy.parrot_mail.ParrotMailPlugin;
import net.slqmy.parrot_mail.parrot.ai.FlyToLocationGoal;
import net.slqmy.parrot_mail.parrot.ai.FlyToPlayerGoal;
import net.slqmy.parrot_mail.parrot.data.ParrotData;
import net.slqmy.parrot_mail.parrot.data.SerializedParrotData;
import net.slqmy.parrot_mail.parrot.journey.JourneyData;
import net.slqmy.parrot_mail.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public final class MailParrot {
    @Getter
    private static final Map<UUID, MailParrot> mailParrots = new HashMap<>();

    private final ParrotMailPlugin plugin;
    private final JourneyData journeyData;

    private org.bukkit.entity.Parrot parrot;
    private net.minecraft.world.entity.animal.Parrot nmsParrot;

    private boolean serialized;
    private SerializedParrotData serializedParrot;

    private ItemDisplay bundle;
    private BundlePositioner bundleLocationUpdater;

    private ItemStack bundleItem;
    private ItemStack guideItem;

    public MailParrot(org.bukkit.entity.Parrot parrot) {
        this.plugin = ParrotMailPlugin.getInstance();
        this.journeyData = new JourneyData();

        this.parrot = parrot;
        this.nmsParrot = Utils.toNMS(parrot);

        loadBundleAndGuideItem();
        readGuideItemAndFlyToTarget();

        mailParrots.put(parrot.getUniqueId(), this);
    }

    private MailParrot(SerializedParrotData serialized) {
        this.plugin = ParrotMailPlugin.getInstance();
        this.journeyData = serialized.journeyData();

        this.serialized = true;
        this.serializedParrot = serialized;

        mailParrots.put(serialized.uuid(), this);
    }

    @Nullable
    public static MailParrot from(org.bukkit.entity.Parrot parrot) {
        return mailParrots.get(parrot.getUniqueId());
    }

    public static MailParrot from(SerializedParrotData serialized) {
        return new MailParrot(serialized);
    }

    public void remove() {
        mailParrots.remove(parrot.getUniqueId());

        removeBundle();
        cancelRunnable();
    }

    public void removeIfInactive() {
        if (bundle == null && guideItem == null) {
            remove();
        }
    }

    public ParrotData toParrotData() {
        return new ParrotData(
            parrot.getUniqueId(),
            parrot.getWorld().getUID(),
            parrot.getChunk().getX(),
            parrot.getChunk().getZ(),
            journeyData.onJourney());
    }

    @SuppressWarnings("deprecation")
    public void serializeAndStore() {
        this.parrot = null;
        this.nmsParrot = null;

        this.serialized = true;
        this.serializedParrot = new SerializedParrotData(
            Base64.getEncoder().encodeToString(Bukkit.getUnsafe().serializeEntity(parrot)),
            journeyData,
            parrot.getWorld().getUID(),
            parrot.getUniqueId());
    }

    @SuppressWarnings("deprecation")
    public void deserializeAndRestore() {
        byte[] decoded = Base64.getDecoder().decode(serializedParrot.base64Encoded());

        this.parrot = (Parrot) Bukkit.getUnsafe().deserializeEntity(decoded, serializedParrot.getWorld(), true);
        this.nmsParrot = Utils.toNMS(parrot);

        this.serialized = false;
        this.serializedParrot = null;

        loadBundleAndGuideItem();
        readGuideItemAndFlyToTarget();
    }

    public boolean hasBundle() {
        return bundle != null;
    }

    public boolean hasGuideItem() {
        return guideItem != null;
    }

    public void giveBundle(Player player, ItemStack bundleItem) {
        player.getInventory().setItemInMainHand(null);

        EntityEquipment parrotEquipment = parrot.getEquipment();
        parrotEquipment.setItemInMainHand(bundleItem);
        parrotEquipment.setDropChance(EquipmentSlot.HAND, 1.0F);

        spawnBundleDisplay();
        removeLandOnShoulderGoal();

        this.bundleLocationUpdater = new BundlePositioner(parrot, bundle);
        this.bundleItem = bundleItem;
    }

    public void removeBundle(Player player) {
        PlayerInventory playerInventory = player.getInventory();

        EntityEquipment parrotEquipment = parrot.getEquipment();
        parrotEquipment.setItemInMainHand(null);

        if (playerInventory.getItemInMainHand().isEmpty()) {
            player.getInventory().setItemInMainHand(bundleItem);
        } else {
            Location location = parrot.getLocation();
            location.getWorld().dropItem(location, bundleItem);
        }

        removeBundle();
        cancelRunnable();

        reapplyDefaultAi();

        this.bundleItem = null;
    }

    public void setGuideItem(Player player, ItemStack guideItem) {
        EntityEquipment parrotEquipment = parrot.getEquipment();
        parrotEquipment.setItemInOffHand(guideItem);
        parrotEquipment.setDropChance(EquipmentSlot.OFF_HAND, 1.0F);

        PlayerInventory playerInventory = player.getInventory();
        playerInventory.setItemInMainHand(null);

        this.guideItem = guideItem;
    }

    public void removeGuideItem(Player player) {
        PlayerInventory playerInventory = player.getInventory();

        EntityEquipment parrotEquipment = parrot.getEquipment();
        parrotEquipment.setItemInOffHand(null);

        if (playerInventory.getItemInMainHand().isEmpty()) {
            player.getInventory().setItemInMainHand(guideItem);
        } else {
            Location parrotLocation = parrot.getLocation();
            parrotLocation.getWorld().dropItem(parrotLocation, guideItem);
        }

        reapplyDefaultAi();
        removeLandOnShoulderGoal();

        this.guideItem = null;
    }

    public void readGuideItemAndFlyToTarget() {
        if (guideItem == null) {
            return;
        }

        switch (guideItem.getType()) {
            case COMPASS -> sendTo(parrot.getWorld().getSpawnLocation());
            case NAME_TAG -> sendTo(Utils.readNameTag(guideItem));
            case MAP -> sendTo(Utils.readMap(guideItem, parrot.getWorld()));
        }
    }

    public void tickJourneyProgress() {
        if (journeyData.chunkLoaded()) {
            journeyData.fakeLocation().add(journeyData.velocity());
        }
    }

    public void removeDefaultAi() {
        nmsParrot.goalSelector.getAvailableGoals().clear();
    }

    public void reapplyDefaultAi() {
        try {
            Method registerGoals = Mob.class.getDeclaredMethod("registerGoals");
            registerGoals.setAccessible(true);

            removeDefaultAi();
            registerGoals.invoke(nmsParrot);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendTo(@Nullable Location location) {
        if (location == null) {
            rejectGuideItem();
        } else {
            journeyData.onJourney(true);
            journeyData.fakeLocation(parrot.getLocation());

            removeDefaultAi();
            nmsParrot.goalSelector.addGoal(0, new FlyToLocationGoal(this, location, 4.0D));
        }
    }

    private void sendTo(@Nullable OfflinePlayer player) {
        if (player == null) {
            rejectGuideItem();
        } else {
            journeyData.onJourney(true);
            journeyData.fakeLocation(parrot.getLocation());
            nmsParrot.goalSelector.addGoal(0, new FlyToPlayerGoal(this, (Player) player, 4.0D));
        }
    }

    private void rejectGuideItem() {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (guideItem != null) {
                Location parrotLocation = parrot.getLocation();
                parrotLocation.getWorld().dropItem(parrotLocation, guideItem);

                this.guideItem = null;
            }
        }, 50L);
    }

    private void loadBundleAndGuideItem() {
        if (Utils.getFirstOrNull(parrot.getPassengers()) instanceof ItemDisplay bundleDisplay) {
            this.bundle = bundleDisplay;
            this.bundleLocationUpdater = new BundlePositioner(parrot, bundle);

            removeLandOnShoulderGoal();
        }

        if (!parrot.getEquipment().getItemInMainHand().isEmpty()) {
            this.bundleItem = parrot.getEquipment().getItemInMainHand();
        }

        if (!parrot.getEquipment().getItemInOffHand().isEmpty()) {
            this.guideItem = parrot.getEquipment().getItemInOffHand();
        }
    }

    private void removeLandOnShoulderGoal() {
        nmsParrot.goalSelector.removeAllGoals((goal) -> goal instanceof LandOnOwnersShoulderGoal);
    }

    @SuppressWarnings("UnstableApiUsage")
    private void spawnBundleDisplay() {
        this.bundle = parrot.getWorld().spawn(
            parrot.getLocation(), ItemDisplay.class);

        bundle.setRotation(0, 0);

        ItemStack bundleItem = new ItemStack(Material.BUNDLE);
        BundleMeta bundleMeta = (BundleMeta) bundleItem.getItemMeta();
        bundleMeta.addItem(new ItemStack(Material.DIRT, 64));
        bundleItem.setItemMeta(bundleMeta);

        bundle.setItemStack(bundleItem);
        bundle.setBillboard(Display.Billboard.FIXED);
        bundle.setInterpolationDelay(0);
        bundle.setInterpolationDuration(0);

        Transformation transformation = bundle.getTransformation();
        transformation.getScale().set(0.5D, 0.5D, 0.5D);
        bundle.setTransformation(transformation);

        parrot.addPassenger(bundle);
    }

    private void removeBundle() {
        if (bundle != null) {
            bundle.remove();
            this.bundle = null;
        }
    }

    private void cancelRunnable() {
        if (bundleLocationUpdater != null) {
            bundleLocationUpdater.cancel();
            this.bundleLocationUpdater = null;
        }
    }
}
