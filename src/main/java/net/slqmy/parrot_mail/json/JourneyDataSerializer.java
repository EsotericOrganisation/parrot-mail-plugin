package net.slqmy.parrot_mail.json;

import com.google.gson.*;
import net.slqmy.parrot_mail.parrot.journey.JourneyData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.lang.reflect.Type;
import java.util.UUID;

public final class JourneyDataSerializer implements JsonSerializer<JourneyData>, JsonDeserializer<JourneyData> {
    @Override
    public JourneyData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        JourneyData journeyData = new JourneyData();

        journeyData.onJourney(jsonObject.get("onJourney").getAsBoolean());
        journeyData.chunkLoaded(jsonObject.get("chunkLoaded").getAsBoolean());
        journeyData.fakeLocation(deserializeLocation(jsonObject.getAsJsonObject("fakeLocation")));
        journeyData.velocity(deserializeVector(jsonObject.getAsJsonArray("velocity")));

        return journeyData;
    }

    @Override
    public JsonElement serialize(JourneyData src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("onJourney", src.onJourney());
        jsonObject.addProperty("chunkLoaded", src.chunkLoaded());
        jsonObject.add("fakeLocation", serializeLocation(src.fakeLocation()));
        jsonObject.add("velocity", serializeVector(src.velocity()));

        return jsonObject;
    }

    private JsonArray serializeVector(Vector vector) {
        JsonArray jsonArray = new JsonArray();

        jsonArray.add(vector.getX());
        jsonArray.add(vector.getY());
        jsonArray.add(vector.getZ());

        return jsonArray;
    }

    private Vector deserializeVector(JsonArray jsonArray) {
        return new Vector(
            jsonArray.get(0).getAsDouble(),
            jsonArray.get(1).getAsDouble(),
            jsonArray.get(2).getAsDouble()
        );
    }

    private JsonObject serializeLocation(Location location) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("world", location.getWorld().getUID().toString());
        jsonObject.addProperty("x", location.getX());
        jsonObject.addProperty("y", location.getY());
        jsonObject.addProperty("z", location.getZ());

        return jsonObject;
    }

    private Location deserializeLocation(JsonObject jsonObject) {
        return new Location(
            Bukkit.getWorld(UUID.fromString(jsonObject.get("world").getAsString())),
            jsonObject.get("x").getAsDouble(),
            jsonObject.get("y").getAsDouble(),
            jsonObject.get("z").getAsDouble()
        );
    }
}
