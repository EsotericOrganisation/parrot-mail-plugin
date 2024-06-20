package net.slqmy.parrot_mail.util;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.world.entity.Entity;
import net.slqmy.parrot_mail.ParrotMailPlugin;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.List;

public final class Utils {
    private static final ParrotMailPlugin plugin = ParrotMailPlugin.getInstance();
    private static final MiniMessage MM = MiniMessage.miniMessage();

    @Nullable
    public static <E> E getFirstOrNull(List<E> list) {
        return list.isEmpty() ? null : list.getFirst();
    }

    public static <T extends Entity> T toNMS(org.bukkit.entity.Entity entity) {
        @SuppressWarnings("unchecked")
        T nms = (T) ((CraftEntity) entity).getHandle();
        return nms;
    }

    public static void writeMap(ItemStack map, Location location) {
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        ItemMeta meta = map.getItemMeta();
        meta.displayName(MM.deserialize("<!i><yellow>Parrot Instructions"));
        meta.lore(List.of(MM.deserialize(String.format("<!i><gray>Fly to %d, %d, %d", x, y, z))));

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(key("map_x"), PersistentDataType.INTEGER, x);
        pdc.set(key("map_y"), PersistentDataType.INTEGER, y);
        pdc.set(key("map_z"), PersistentDataType.INTEGER, z);

        map.setItemMeta(meta);
    }

    public static Location readMap(ItemStack map, World world) {
        ItemMeta meta = map.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        Integer x = pdc.get(key("map_x"), PersistentDataType.INTEGER);
        Integer y = pdc.get(key("map_y"), PersistentDataType.INTEGER);
        Integer z = pdc.get(key("map_z"), PersistentDataType.INTEGER);

        if (x == null || y == null || z == null) {
            return null;
        }

        return new Location(world, x, y, z);
    }

    public static void writeNameTag(ItemStack nameTag, String name) {
        ItemMeta meta = nameTag.getItemMeta();
        meta.displayName(MM.deserialize("<!i><yellow>Parrot Instructions"));
        meta.lore(List.of(MM.deserialize("<!i><gray>Fly to player " + name)));

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(key("name"), PersistentDataType.STRING, name);

        nameTag.setItemMeta(meta);
    }

    public static OfflinePlayer readNameTag(ItemStack nameTag) {
        ItemMeta meta = nameTag.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        String name = pdc.get(key("name"), PersistentDataType.STRING);

        if (name == null) {
            return null;
        }

        return plugin.getServer().getOfflinePlayer(name);
    }

    public static <T> T reflectField(Class<?> clazz, String name, Object instance) {
        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);

            @SuppressWarnings("unchecked")
            T value = (T) field.get(instance);
            return value;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T invokeReflectedMethod(Class<?> clazz, String methodName, Object instance, Object... args) {
        try {
            Class<?>[] argTypes = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                argTypes[i] = args[i].getClass();
            }

            java.lang.reflect.Method method = clazz.getDeclaredMethod(methodName, argTypes);
            method.setAccessible(true);

            @SuppressWarnings("unchecked")
            T value = (T) method.invoke(instance, args);
            return value;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private static NamespacedKey key(String key) {
        return new NamespacedKey(plugin, key);
    }
}
