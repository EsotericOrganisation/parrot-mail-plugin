package net.slqmy.parrot_mail.parrot.data;

import net.slqmy.parrot_mail.parrot.MailParrot;
import net.slqmy.parrot_mail.parrot.journey.JourneyData;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.UUID;

public record SerializedParrotData(
  String base64Encoded,
  JourneyData journeyData,
  UUID worldId,
  UUID uuid
) {
    public void markAsMailParrot() {
        MailParrot.from(this);
    }

    public World getWorld() {
        return Bukkit.getWorld(worldId);
    }
}
