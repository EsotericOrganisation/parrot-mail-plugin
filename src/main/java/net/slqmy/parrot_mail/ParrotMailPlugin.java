package net.slqmy.parrot_mail;

import net.slqmy.parrot_mail.event.ParrotRightClickListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class ParrotMailPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        PluginManager pluginManager = Bukkit.getPluginManager();

        pluginManager.registerEvents(new ParrotRightClickListener(), this);
    }
}
