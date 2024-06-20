package net.slqmy.parrot_mail.parrot.data;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Parrot;

import java.util.UUID;
import java.util.function.Consumer;

public record ParrotData(
    UUID uuid,
    UUID worldId,
    int chunkX,
    int chunkZ,
    boolean onJourney
) {
    public Parrot getParrot() {
        return (Parrot) Bukkit.getEntity(uuid);
    }

    public void loadChunkThen(Consumer<Chunk> consumer) {
        World world = Bukkit.getWorld(worldId);
        assert world != null;

        if (world.isChunkLoaded(chunkX, chunkZ)) {
            consumer.accept(world.getChunkAt(chunkX, chunkZ));
        } else {
            world.getChunkAtAsync(chunkX, chunkZ, consumer.andThen(Chunk::unload));
        }
    }
}
