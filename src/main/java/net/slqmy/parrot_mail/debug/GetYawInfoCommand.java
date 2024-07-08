package net.slqmy.parrot_mail.debug;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.slqmy.parrot_mail.ParrotMailPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public final class GetYawInfoCommand implements CommandExecutor, TabCompleter {
    private final ParrotMailPlugin plugin;

    public GetYawInfoCommand() {
        this.plugin = ParrotMailPlugin.getInstance();
    }

    public void register() {
        PluginCommand command = plugin.getCommand("getyaw");

        assert command != null;
        command.setExecutor(this);
        command.setTabCompleter(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        LivingEntity entity = (LivingEntity) Bukkit.getEntity(UUID.fromString(args[0]));

        assert entity != null;
        net.minecraft.world.entity.LivingEntity nmsEntity = ((CraftLivingEntity) entity).getHandle();

        player.sendMessage(MiniMessage.miniMessage().deserialize(
            "<light_purple>Old: <aqua>" + nmsEntity.yBodyRotO + ", <light_purple>New: <aqua>" + nmsEntity.yBodyRot + ", <light_purple>Head: <aqua>" + nmsEntity.yHeadRot + ", <light_purple>Old Head: <aqua>" + nmsEntity.yHeadRotO
        ));

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        LivingEntity entity = (LivingEntity) player.getTargetEntity(5);

        if (entity == null) return List.of();
        return List.of(entity.getUniqueId().toString());
    }
}
