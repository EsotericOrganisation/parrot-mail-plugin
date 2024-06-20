package net.slqmy.parrot_mail.debug;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.slqmy.parrot_mail.ParrotMailPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class ToggleDebugCommand implements CommandExecutor {
    private final ParrotMailPlugin plugin;
    public ToggleDebugCommand() {
        this.plugin = ParrotMailPlugin.getInstance();
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        plugin.setDebugging(!plugin.isDebugging());

        if (sender instanceof Player player) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(
                "<gray>Debugging is now " + (plugin.isDebugging() ? "<green>enabled" : "<red>disabled") + "<gray>!"));
        }

        return true;
    }

    public void register() {
        PluginCommand command = plugin.getCommand("debug");

        assert command != null;
        command.setExecutor(this);
    }
}
