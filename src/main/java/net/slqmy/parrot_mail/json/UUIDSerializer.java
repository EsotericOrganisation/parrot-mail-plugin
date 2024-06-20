package net.slqmy.parrot_mail.json;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.UUID;

public final class UUIDSerializer implements JsonSerializer<UUID>, JsonDeserializer<UUID> {
    @Override
    public UUID deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return UUID.fromString(json.getAsString());
    }

    @Override
    public JsonElement serialize(UUID uuid, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(uuid.toString());
    }
}
