package net.slqmy.parrot_mail.debug;

import net.kyori.adventure.text.Component;
import net.slqmy.parrot_mail.ParrotMailPlugin;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

public final class LeftRightRotationTestCommand implements CommandExecutor {
    private final ParrotMailPlugin plugin;

    public LeftRightRotationTestCommand() {
        this.plugin = ParrotMailPlugin.getInstance();
    }

    public void register() {
        PluginCommand command = plugin.getCommand("leftrightrot");

        assert command != null;
        command.setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;

        ItemDisplay itemDisplay = player.getWorld().spawn(player.getLocation().add(-2, 0, 0), ItemDisplay.class);
        itemDisplay.setItemStack(new ItemStack(Material.DIAMOND_BLOCK));
        itemDisplay.setBillboard(Display.Billboard.FIXED);

        ItemDisplay display2 = player.getWorld().spawn(player.getLocation().add(2, 0, 0), ItemDisplay.class);
        display2.setItemStack(new ItemStack(Material.NETHERITE_BLOCK));
        display2.setBillboard(Display.Billboard.FIXED);

        BukkitScheduler scheduler = plugin.getServer().getScheduler();

        scheduler.runTaskLater(plugin, () -> {
            player.sendMessage(Component.text("Rotating diamond block left half a revolution!"));

            itemDisplay.setTransformation(new Transformation(
                new Vector3f(),
                new AxisAngle4f(180.0F, 0.0F, 1.0F, 0.0F),
                new Vector3f(2, 2, 2),
                new AxisAngle4f()));

            itemDisplay.setInterpolationDelay(0);
            itemDisplay.setInterpolationDuration(60);
        }, 20L);

        scheduler.runTaskLater(plugin, () -> {
            player.sendMessage(Component.text("Rotating netherite block right and left half a revolution!"));

            display2.setTransformation(new Transformation(
                new Vector3f(),
                new AxisAngle4f(180.0F, 0.0F, 1.0F, 0.0F),
                new Vector3f(2, 2, 2),
                new AxisAngle4f(180.0F, 0.0F, 1.0F, 0.0F)));

            display2.setInterpolationDelay(0);
            display2.setInterpolationDuration(60);
        }, 100L);

        return true;
    }
}
