package dev.alexandrehemery.cobblemonchatalerts;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;

public final class CobblemonChatAlertsModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return this::createConfigScreen;
    }

    private Screen createConfigScreen(Screen parent) {
        CobblemonChatAlertsConfig.Config config = CobblemonChatAlertsConfig.get();
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(text("config.title"))
                .setSavingRunnable(CobblemonChatAlertsConfig::save);
        ConfigEntryBuilder entries = builder.entryBuilder();

        ConfigCategory alerts = builder.getOrCreateCategory(text("config.category.alerts"));
        alerts.addEntry(entries.startBooleanToggle(text("config.alert_rare_spawns"), config.alertRareSpawns)
                .setDefaultValue(true)
                .setSaveConsumer(value -> config.alertRareSpawns = value)
                .build());
        alerts.addEntry(entries.startBooleanToggle(text("config.alert_rare_captures"), config.alertRareCaptures)
                .setDefaultValue(true)
                .setSaveConsumer(value -> config.alertRareCaptures = value)
                .build());
        alerts.addEntry(entries.startBooleanToggle(text("config.alert_rare_defeats"), config.alertRareDefeats)
                .setDefaultValue(true)
                .setSaveConsumer(value -> config.alertRareDefeats = value)
                .build());
        alerts.addEntry(entries.startBooleanToggle(text("config.alert_level_100"), config.alertLevel100)
                .setDefaultValue(true)
                .setSaveConsumer(value -> config.alertLevel100 = value)
                .build());
        alerts.addEntry(entries.startBooleanToggle(text("config.alert_evolution_ready"), config.alertEvolutionReady)
                .setDefaultValue(true)
                .setSaveConsumer(value -> config.alertEvolutionReady = value)
                .build());
        alerts.addEntry(entries.startBooleanToggle(text("config.alert_evolution_complete"), config.alertEvolutionComplete)
                .setDefaultValue(true)
                .setSaveConsumer(value -> config.alertEvolutionComplete = value)
                .build());
        alerts.addEntry(entries.startBooleanToggle(text("config.alert_pokedex"), config.alertPokedex)
                .setDefaultValue(true)
                .setSaveConsumer(value -> config.alertPokedex = value)
                .build());

        ConfigCategory rareFilters = builder.getOrCreateCategory(text("config.category.rare_filters"));
        rareFilters.addEntry(entries.startBooleanToggle(text("config.rare_filter_shiny"), config.rareFilterShiny)
                .setDefaultValue(true)
                .setSaveConsumer(value -> config.rareFilterShiny = value)
                .build());
        rareFilters.addEntry(entries.startBooleanToggle(text("config.rare_filter_legendary"), config.rareFilterLegendary)
                .setDefaultValue(true)
                .setSaveConsumer(value -> config.rareFilterLegendary = value)
                .build());
        rareFilters.addEntry(entries.startBooleanToggle(text("config.rare_filter_mythical"), config.rareFilterMythical)
                .setDefaultValue(true)
                .setSaveConsumer(value -> config.rareFilterMythical = value)
                .build());
        rareFilters.addEntry(entries.startBooleanToggle(text("config.rare_filter_restricted"), config.rareFilterRestricted)
                .setDefaultValue(true)
                .setSaveConsumer(value -> config.rareFilterRestricted = value)
                .build());
        rareFilters.addEntry(entries.startBooleanToggle(text("config.rare_filter_ultra_beast"), config.rareFilterUltraBeast)
                .setDefaultValue(true)
                .setSaveConsumer(value -> config.rareFilterUltraBeast = value)
                .build());
        rareFilters.addEntry(entries.startBooleanToggle(text("config.rare_filter_perfect_ivs"), config.rareFilterPerfectIvs)
                .setDefaultValue(false)
                .setSaveConsumer(value -> config.rareFilterPerfectIvs = value)
                .build());
        rareFilters.addEntry(entries.startBooleanToggle(text("config.rare_filter_high_ivs"), config.rareFilterHighIvs)
                .setDefaultValue(false)
                .setSaveConsumer(value -> config.rareFilterHighIvs = value)
                .build());
        rareFilters.addEntry(entries.startIntField(text("config.high_iv_threshold_percent"), config.highIvThresholdPercent)
                .setDefaultValue(90)
                .setMin(1)
                .setMax(100)
                .setSaveConsumer(value -> config.highIvThresholdPercent = value)
                .build());

        ConfigCategory rareSpawns = builder.getOrCreateCategory(text("config.category.rare_spawns"));
        rareSpawns.addEntry(entries.startEnumSelector(text("config.rare_spawn_audience"), CobblemonChatAlertsConfig.SpawnAudience.class, config.rareSpawnAudience)
                .setDefaultValue(CobblemonChatAlertsConfig.SpawnAudience.BLOCK_RADIUS)
                .setEnumNameProvider(value -> text("config.rare_spawn_audience." + value.name().toLowerCase(Locale.ROOT)))
                .setSaveConsumer(value -> config.rareSpawnAudience = value)
                .build());
        rareSpawns.addEntry(entries.startIntField(text("config.rare_spawn_radius_blocks"), config.rareSpawnRadiusBlocks)
                .setDefaultValue(128)
                .setMin(1)
                .setMax(10_000)
                .setSaveConsumer(value -> config.rareSpawnRadiusBlocks = value)
                .build());
        rareSpawns.addEntry(entries.startIntField(text("config.rare_spawn_radius_chunks"), config.rareSpawnRadiusChunks)
                .setDefaultValue(8)
                .setMin(1)
                .setMax(625)
                .setSaveConsumer(value -> config.rareSpawnRadiusChunks = value)
                .build());
        rareSpawns.addEntry(entries.startBooleanToggle(text("config.show_coordinates"), config.showCoordinates)
                .setDefaultValue(true)
                .setSaveConsumer(value -> config.showCoordinates = value)
                .build());
        rareSpawns.addEntry(entries.startBooleanToggle(text("config.clickable_coordinates"), config.clickableCoordinates)
                .setDefaultValue(true)
                .setSaveConsumer(value -> config.clickableCoordinates = value)
                .build());
        rareSpawns.addEntry(entries.startBooleanToggle(text("config.show_biome"), config.showBiome)
                .setDefaultValue(false)
                .setSaveConsumer(value -> config.showBiome = value)
                .build());
        rareSpawns.addEntry(entries.startBooleanToggle(text("config.show_level"), config.showLevel)
                .setDefaultValue(true)
                .setSaveConsumer(value -> config.showLevel = value)
                .build());
        rareSpawns.addEntry(entries.startBooleanToggle(text("config.show_ivs"), config.showIvs)
                .setDefaultValue(false)
                .setSaveConsumer(value -> config.showIvs = value)
                .build());

        return builder.build();
    }

    private static Component text(String key) {
        return Component.translatable("text." + CobblemonChatAlerts.MOD_ID + "." + key);
    }
}
