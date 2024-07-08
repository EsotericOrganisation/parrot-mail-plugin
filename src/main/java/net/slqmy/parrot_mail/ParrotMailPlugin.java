package net.slqmy.parrot_mail;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.Setter;
import net.slqmy.parrot_mail.debug.DisplayTestCommand;
import net.slqmy.parrot_mail.debug.GetYawInfoCommand;
import net.slqmy.parrot_mail.debug.ToggleDebugCommand;
import net.slqmy.parrot_mail.event.ParrotLoadOrUnloadListener;
import net.slqmy.parrot_mail.event.ParrotRemoveListener;
import net.slqmy.parrot_mail.event.ParrotRightClickListener;
import net.slqmy.parrot_mail.event.TickEndListener;
import net.slqmy.parrot_mail.json.JourneyDataSerializer;
import net.slqmy.parrot_mail.json.UUIDSerializer;
import net.slqmy.parrot_mail.parrot.MailParrot;
import net.slqmy.parrot_mail.parrot.data.ParrotData;
import net.slqmy.parrot_mail.parrot.data.SerializedParrotData;
import net.slqmy.parrot_mail.parrot.journey.JourneyData;
import org.bukkit.entity.Parrot;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ParrotMailPlugin extends JavaPlugin implements Listener {
    @Getter
    private static ParrotMailPlugin instance;

    private SavedParrots savedParrots;

    @Getter @Setter
    private boolean isDebugging;

    public ParrotMailPlugin() {
        ParrotMailPlugin.instance = this;

        this.savedParrots = new SavedParrots(new ArrayList<>(), new ArrayList<>());
        this.isDebugging = false;
        this.setNaggable(false);
    }

    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        loadConfig();
        loadMailParrots();
        initialiseMailParrots();

        getLogger().info("There are " + savedParrots.size() + " active mail parrots.");
        getLogger().info(savedParrots.serializedMailParrots().size() + " of those are travelling silently.");
    }

    @Override
    public void onEnable() {
        new ParrotRightClickListener().register();
        new ParrotRemoveListener().register();
        new ParrotLoadOrUnloadListener().register();
        new TickEndListener().register();

        new ToggleDebugCommand().register();
        new GetYawInfoCommand().register();
        new DisplayTestCommand().register();

        getServer().getPluginManager().registerEvents(this, this);
        super.onEnable();
    }

    @Override
    public void onDisable() {
        findMailParrots();
        saveMailParrots();

        getLogger().info("There are " + savedParrots.size() + " active mail parrots.");
        super.onDisable();
    }

    private void loadConfig() {
        if (!getDataFolder().exists()) {
            boolean success = getDataFolder().mkdir();
            getLogger().info("New data folder created: " + success);
        }

        saveResource("parrots.json", false);
    }

    private void loadMailParrots() {
        File parrotFile = new File(getDataFolder(), "parrots.json");

        try {
            FileReader reader = new FileReader(parrotFile);
            Gson gson = createGson();

            this.savedParrots = gson.fromJson(reader, SavedParrots.class);

            reader.close();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private void initialiseMailParrots() {
        List<ParrotData> corrupted = new ArrayList<>();

        for (ParrotData data : savedParrots.idleMailParrots()) {
            data.loadChunkThen(chunk -> {
                Parrot parrot = data.getParrot();

                if (parrot == null) {
                    corrupted.add(data);
                    return;
                }

                new MailParrot(parrot);
            });
        }

        for (SerializedParrotData data : savedParrots.serializedMailParrots()) {
            data.markAsMailParrot();
        }

        savedParrots.idleMailParrots().removeAll(corrupted);
    }

    private void findMailParrots() {
        savedParrots.clear();

        for (MailParrot mailParrot : MailParrot.getMailParrots().values()) {
            if (mailParrot.isSerialized()) {
                savedParrots.serializedMailParrots().add(mailParrot.getSerializedParrot());
            } else {
                savedParrots.idleMailParrots().add(mailParrot.toParrotData());
            }
        }
    }

    private void saveMailParrots() {
        File parrotFile = new File(getDataFolder(), "parrots.json");

        try {
            FileWriter writer = new FileWriter(parrotFile);
            Gson gson = createGson();

            gson.toJson(savedParrots, writer);

            writer.close();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private Gson createGson() {
        return new GsonBuilder()
            .registerTypeAdapter(JourneyData.class, new JourneyDataSerializer())
            .registerTypeAdapter(UUID.class, new UUIDSerializer())
            .setPrettyPrinting()
            .create();
    }

    private record SavedParrots(
        List<ParrotData> idleMailParrots,
        List<SerializedParrotData> serializedMailParrots
    ) {
        public int size() {
            return idleMailParrots.size() + serializedMailParrots.size();
        }

        public void clear() {
            idleMailParrots.clear();
            serializedMailParrots.clear();
        }
    }
}
