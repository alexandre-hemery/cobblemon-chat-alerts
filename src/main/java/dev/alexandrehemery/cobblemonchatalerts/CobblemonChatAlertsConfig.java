package dev.alexandrehemery.cobblemonchatalerts;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CobblemonChatAlertsConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("cobblemon-chat-alerts.json");
    private static Config config = new Config();

    private CobblemonChatAlertsConfig() {
    }

    public static Config get() {
        return config;
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
            Config loaded = GSON.fromJson(reader, Config.class);
            config = loaded == null ? new Config() : loaded;
            config.clamp();
        } catch (Exception exception) {
            CobblemonChatAlerts.LOGGER.warn("Could not read config {}, using defaults.", CONFIG_PATH, exception);
            config = new Config();
        }

        save();
    }

    public static void save() {
        config.clamp();
        try {
            Path parent = CONFIG_PATH.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException exception) {
            CobblemonChatAlerts.LOGGER.warn("Could not save config {}.", CONFIG_PATH, exception);
        }
    }

    public enum SpawnAudience {
        BLOCK_RADIUS,
        CHUNK_RADIUS,
        GLOBAL
    }

    public static final class Config {
        public boolean alertRareSpawns = true;
        public boolean alertRareCaptures = true;
        public boolean alertRareDefeats = true;
        public boolean alertLevel100 = true;
        public boolean alertEvolutionReady = true;
        public boolean alertEvolutionComplete = true;
        public boolean alertPokedex = true;

        public boolean rareFilterShiny = true;
        public boolean rareFilterLegendary = true;
        public boolean rareFilterMythical = true;
        public boolean rareFilterRestricted = true;
        public boolean rareFilterUltraBeast = true;
        public boolean rareFilterPerfectIvs = false;
        public boolean rareFilterHighIvs = false;
        public int highIvThresholdPercent = 90;

        public SpawnAudience rareSpawnAudience = SpawnAudience.BLOCK_RADIUS;
        public int rareSpawnRadiusBlocks = 128;
        public int rareSpawnRadiusChunks = 8;
        public boolean showCoordinates = true;
        public boolean clickableCoordinates = true;
        public boolean showBiome = false;
        public boolean showLevel = true;
        public boolean showIvs = false;

        private void clamp() {
            if (rareSpawnAudience == null) {
                rareSpawnAudience = SpawnAudience.BLOCK_RADIUS;
            }
            rareSpawnRadiusBlocks = clamp(rareSpawnRadiusBlocks, 1, 10_000);
            rareSpawnRadiusChunks = clamp(rareSpawnRadiusChunks, 1, 625);
            highIvThresholdPercent = clamp(highIvThresholdPercent, 1, 100);
        }

        private static int clamp(int value, int min, int max) {
            return Math.max(min, Math.min(max, value));
        }
    }
}
