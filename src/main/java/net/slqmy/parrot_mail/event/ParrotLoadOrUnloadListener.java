package net.slqmy.parrot_mail.event;

import net.slqmy.parrot_mail.ParrotMailPlugin;
import net.slqmy.parrot_mail.parrot.MailParrot;
import net.slqmy.parrot_mail.parrot.journey.JourneyData;
import org.bukkit.Chunk;
import org.bukkit.entity.Parrot;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

public final class ParrotLoadOrUnloadListener implements Listener {
    private final ParrotMailPlugin plugin;

    public ParrotLoadOrUnloadListener() {
        this.plugin = ParrotMailPlugin.getInstance();
    }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Chunk chunk = event.getChunk();

        for (MailParrot mailParrot : MailParrot.getMailParrots().values()) {
            if (!mailParrot.isSerialized()) {
                continue;
            }

            JourneyData journeyData = mailParrot.getJourneyData();

            if (chunk.equals(journeyData.fakeLocation().getChunk())) {
                journeyData.chunkLoaded(true);
                mailParrot.deserializeAndRestore();
                mailParrot.getParrot().spawnAt(journeyData.fakeLocation());
            }
        }
    }

    @EventHandler
    @SuppressWarnings("removal")
    public void onParrotUnload(org.bukkit.event.entity.EntityRemoveEvent event) {
        if (plugin.getServer().isStopping()) {
            return;
        }

        if (event.getCause() != org.bukkit.event.entity.EntityRemoveEvent.Cause.UNLOAD) {
            return;
        }

        if (!(event.getEntity() instanceof Parrot parrot)) {
            return;
        }

        MailParrot mailParrot = MailParrot.from(parrot);
        if (mailParrot == null) {
            return;
        }

        JourneyData journeyData = mailParrot.getJourneyData();

        if (!journeyData.onJourney()) {
            return;
        }

        journeyData.chunkLoaded(false);

        mailParrot.serializeAndStore();
        parrot.remove();
    }
}
