package net.slqmy.parrot_mail.runnables;

import net.slqmy.parrot_mail.MailParrotUtils;
import org.bukkit.Location;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Parrot;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class BundlePositionUpdater extends BukkitRunnable {
    private final Parrot parrot;
    private final ItemDisplay bundle;
    private Location lastParrotLocation;
    private int ticksSinceLastTP;
    public BundlePositionUpdater(@NotNull Parrot parrot, @NotNull ItemDisplay bundle) {
        this.parrot = parrot;
        this.bundle = bundle;
        this.lastParrotLocation = parrot.getLocation();
        this.ticksSinceLastTP = 0;
    }
    @Override
    public void run() {
        boolean shouldTP = ticksSinceLastTP >= 100;

        if (shouldTP) this.ticksSinceLastTP = 0;
        else this.ticksSinceLastTP++;

        this.lastParrotLocation = MailParrotUtils.updateBundlePosition(this.parrot, this.bundle, this.lastParrotLocation, shouldTP);
    }
}
