package top.offsetmonkey538.lanwhitelist.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.WorldSavePath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static top.offsetmonkey538.lanwhitelist.LANWhitelist.LOGGER;
import static top.offsetmonkey538.lanwhitelist.LANWhitelist.MOD_ID;

public final class WhitelistEnabled {
    private WhitelistEnabled() {

    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // Always loads from disk
    public static boolean isWhitelistEnabled(final IntegratedServer server) {
        try {
            return GSON.fromJson(Files.newBufferedReader(getSavePath(server)), JsonData.class).enabled();
        } catch (IOException e) {
            LOGGER.error("Failed to load whitelist enabled file! Whitelist will be disabled.", e);
            return false;
        }
    }

    // Always write to disk
    public static void setWhitelistEnabled(final IntegratedServer server, boolean enabled) {
        try {
            final Path savePath = getSavePath(server);
            Files.createDirectories(savePath.getParent());
            Files.writeString(savePath, GSON.toJson(new JsonData(enabled)));
        } catch (IOException e) {
            LOGGER.error("Failed to write whitelist enabled file!", e);
        }
    }

    private static Path getSavePath(final IntegratedServer server) {
        return server.getSavePath(WorldSavePath.ROOT).resolve(MOD_ID).resolve("enabled.json");
    }

    private record JsonData(boolean enabled) {

    }
}
