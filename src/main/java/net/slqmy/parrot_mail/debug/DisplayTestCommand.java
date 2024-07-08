package net.slqmy.parrot_mail.debug;

import net.kyori.adventure.text.Component;
import net.slqmy.parrot_mail.ParrotMailPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class DisplayTestCommand implements CommandExecutor {
    private final ParrotMailPlugin plugin;

    public DisplayTestCommand() {
        this.plugin = ParrotMailPlugin.getInstance();
    }

    public void register() {
        PluginCommand command = plugin.getCommand("displaytest");
        assert command != null;

        command.setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        Player player = (Player) sender;
        Location location = player.getLocation();
        location.setYaw(0);
        location.setPitch(0);

        ItemDisplay display = player.getWorld().spawn(location, ItemDisplay.class);
        display.setItemStack(new ItemStack(Material.DIAMOND_AXE));

        display.setInterpolationDelay(0);
        display.setInterpolationDuration(50);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            Bukkit.broadcast(Component.text("DisplayTestCommand: Rotating display!"));

            display.setTransformationMatrix(new Matrix4f().rotateY((float) Math.toRadians(179)));
            display.setInterpolationDelay(0);
            display.setInterpolationDuration(50);
        }, 40L);

        plugin.getServer().getScheduler().runTaskLater(plugin, task -> {
            Bukkit.broadcast(Component.text("DisplayTestCommand: Rotating display mid-rotation!"));

            display.setTransformationMatrix(new Matrix4f().rotateY((float) Math.toRadians(-90)));
            display.setInterpolationDelay(0);
            display.setInterpolationDuration(50);
        }, 80L);
        return true;
    }
}
