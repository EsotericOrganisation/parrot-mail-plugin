package net.slqmy.parrot_mail.parrot.journey;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.Location;
import org.bukkit.util.Vector;

@Getter @Setter @Accessors(fluent = true)
public final class JourneyData {
    private boolean onJourney = false;
    private boolean chunkLoaded = true;
    private Location fakeLocation = null;
    private Vector velocity = null;

    public void reset() {
        onJourney = false;
        chunkLoaded = true;
        fakeLocation = null;
        velocity = null;
    }
}
