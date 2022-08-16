package com.illtamer.infinite.bot.minecraft.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.illtamer.infinite.bot.api.Pair;
import com.illtamer.infinite.bot.api.util.HttpRequestUtil;
import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@UtilityClass
public class ValidUtil {
    private static final String SESSION_SERVER = "https://sessionserver.mojang.com/session/minecraft/profile/";
    private static final Gson GSON = new Gson();

    public static boolean isValidPlayer(@NotNull Player player) {
        return isValidUUID(player.getUniqueId(), player.getName());
    }

    public static boolean isValidUUID(@NotNull UUID uuid, @NotNull String name) {
        return isValidUUID(uuid.toString(), name);
    }

    public static boolean isValidUUID(@NotNull String uuid, @NotNull String name) {
        final Pair<Integer, String> pair = HttpRequestUtil.getJson(SESSION_SERVER + uuid, null);
        if (pair.getKey() != 200) return false;
        final JsonObject object = GSON.fromJson(pair.getValue(), JsonObject.class);
        return name.equals(object.get("name").getAsString());
    }

}
