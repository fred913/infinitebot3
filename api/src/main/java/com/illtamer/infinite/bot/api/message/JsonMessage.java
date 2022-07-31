package com.illtamer.infinite.bot.api.message;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.illtamer.infinite.bot.api.exception.ExclusiveMessageException;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class JsonMessage extends Message {

    private final JsonArray array;

    private boolean textOnly = true;

    public JsonMessage() {
        this.array = new JsonArray();
    }

    private JsonMessage(JsonArray array, boolean textOnly) {
        this.array = array;
        this.textOnly = textOnly;
    }

    @Override
    protected void add(String type, Map<String, @Nullable Object> data) {
        if (textOnly)
            textOnly = "text".equals(type);
        JsonObject dataJson = new JsonObject();
        data.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .forEach(entry -> {
                    Object value = entry.getValue();
                    JsonElement element;
                    if (value instanceof Message)
                        element = ((JsonMessage) value).array;
                    else
                        element = new JsonPrimitive(value.toString());
                    dataJson.add(entry.getKey(), element);
                });
        if (dataJson.size() == 0) return;

        JsonObject node = new JsonObject();
        node.add("type", new JsonPrimitive(type));
        node.add("data", dataJson);
        array.add(node);
    }

    @Override
    protected void addExclusive(String type, Map<String, @Nullable Object> data) {
        if (!array.isEmpty()) {
            for (JsonElement jsonElement : array) {
                JsonObject object = (JsonObject) jsonElement;
                if (!object.get("type").getAsString().equals(type))
                    throw new ExclusiveMessageException(type);
            }
        }
        add(type, data);
    }

    @Override
    public boolean isTextOnly() {
        return textOnly;
    }

    @Override
    public JsonMessage clone() {
        return new JsonMessage(array, textOnly);
    }

    @Override
    public String toString() {
        return array.toString();
    }

}
