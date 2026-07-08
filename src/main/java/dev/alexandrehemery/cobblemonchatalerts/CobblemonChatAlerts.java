package dev.alexandrehemery.cobblemonchatalerts;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.ActorType;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.battles.BattleFaintedEvent;
import com.cobblemon.mod.common.api.events.entity.SpawnEvent;
import com.cobblemon.mod.common.api.events.pokemon.LevelUpEvent;
import com.cobblemon.mod.common.api.events.pokemon.PokedexDataChangedEvent;
import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent;
import com.cobblemon.mod.common.api.events.pokemon.evolution.EvolutionCompleteEvent;
import com.cobblemon.mod.common.api.events.pokemon.evolution.EvolutionTestedEvent;
import com.cobblemon.mod.common.api.pokedex.PokedexEntryProgress;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.IVs;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CobblemonChatAlerts implements ModInitializer {
    public static final String MOD_ID = "cobblemon_chat_alerts";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final long EVOLUTION_READY_COOLDOWN_MS = 60_000L;
    private static final Set<String> RARE_LABELS = Set.of(
            "legendary",
            "mythical",
            "restricted",
            "sublegendary",
            "ultra_beast",
            "ultra-beast",
            "ultrabeast"
    );
    private static final Map<UUID, Long> EVOLUTION_READY_NOTIFIED_AT = new ConcurrentHashMap<>();

    private static MinecraftServer currentServer;

    @Override
    public void onInitialize() {
        CobblemonChatAlertsConfig.load();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> currentServer = server);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            if (currentServer == server) {
                currentServer = null;
            }
            EVOLUTION_READY_NOTIFIED_AT.clear();
        });

        CobblemonEvents.POKEMON_ENTITY_SPAWN.subscribe(CobblemonChatAlerts::onPokemonSpawn);
        CobblemonEvents.POKEMON_CAPTURED.subscribe(CobblemonChatAlerts::onPokemonCaptured);
        CobblemonEvents.BATTLE_FAINTED.subscribe(CobblemonChatAlerts::onBattleFainted);
        CobblemonEvents.LEVEL_UP_EVENT.subscribe(CobblemonChatAlerts::onLevelUp);
        CobblemonEvents.EVOLUTION_TESTED.subscribe(CobblemonChatAlerts::onEvolutionTested);
        CobblemonEvents.EVOLUTION_COMPLETE.subscribe(CobblemonChatAlerts::onEvolutionComplete);
        CobblemonEvents.POKEDEX_DATA_CHANGED_POST.subscribe(CobblemonChatAlerts::onPokedexDataChanged);
    }

    private static void onPokemonSpawn(SpawnEvent<PokemonEntity> event) {
        CobblemonChatAlertsConfig.Config config = CobblemonChatAlertsConfig.get();
        if (!config.alertRareSpawns) {
            return;
        }

        PokemonEntity entity = event.getEntity();
        Pokemon pokemon = entity.getPokemon();
        if (pokemon.getOwnerUUID() != null) {
            return;
        }

        List<Component> rareReasons = rareReasons(pokemon, config);
        if (rareReasons.isEmpty()) {
            return;
        }

        Level level = entity.level();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        BlockPos pos = entity.blockPosition();
        Component message = alert(
                "spawn_rare",
                ChatFormatting.GOLD,
                pokemonName(pokemon),
                join(rareReasons),
                rareSpawnDetails(pokemon, serverLevel, pos, config)
        );

        sendToSpawnAudience(serverLevel, entity, message, config);
    }

    private static void onPokemonCaptured(PokemonCapturedEvent event) {
        CobblemonChatAlertsConfig.Config config = CobblemonChatAlertsConfig.get();
        if (!config.alertRareCaptures) {
            return;
        }

        Pokemon pokemon = event.getPokemon();
        List<Component> rareReasons = rareReasons(pokemon, config);
        if (rareReasons.isEmpty()) {
            return;
        }

        send(event.getPlayer(), alert("capture_rare", ChatFormatting.GOLD, pokemonName(pokemon), join(rareReasons), rarePokemonDetails(pokemon, config)));
    }

    private static void onBattleFainted(BattleFaintedEvent event) {
        CobblemonChatAlertsConfig.Config config = CobblemonChatAlertsConfig.get();
        if (!config.alertRareDefeats) {
            return;
        }

        BattlePokemon battlePokemon = event.getKilled();
        if (battlePokemon.getActor().getType() != ActorType.WILD) {
            return;
        }

        Pokemon pokemon = battlePokemon.getOriginalPokemon();
        List<Component> rareReasons = rareReasons(pokemon, config);
        if (rareReasons.isEmpty()) {
            return;
        }

        PokemonBattle battle = event.getBattle();
        Component message = alert("defeat_rare", ChatFormatting.GOLD, pokemonName(pokemon), join(rareReasons), rarePokemonDetails(pokemon, config));

        for (ServerPlayer player : battle.getPlayers()) {
            send(player, message);
        }
    }

    private static void onLevelUp(LevelUpEvent event) {
        CobblemonChatAlertsConfig.Config config = CobblemonChatAlertsConfig.get();
        if (!config.alertLevel100) {
            return;
        }

        if (event.getOldLevel() >= 100 || event.getNewLevel() < 100) {
            return;
        }

        Pokemon pokemon = event.getPokemon();
        sendToOwner(pokemon, alert("level_100", ChatFormatting.LIGHT_PURPLE, pokemonName(pokemon)));
    }

    private static void onEvolutionTested(EvolutionTestedEvent event) {
        CobblemonChatAlertsConfig.Config config = CobblemonChatAlertsConfig.get();
        if (!config.alertEvolutionReady) {
            return;
        }

        if (!event.getResult()) {
            return;
        }

        Pokemon pokemon = event.getPokemon();
        UUID uuid = pokemon.getUuid();
        long now = System.currentTimeMillis();
        Long lastNotification = EVOLUTION_READY_NOTIFIED_AT.get(uuid);
        if (lastNotification != null && now - lastNotification < EVOLUTION_READY_COOLDOWN_MS) {
            return;
        }

        EVOLUTION_READY_NOTIFIED_AT.put(uuid, now);
        sendToOwner(pokemon, alert("evolution_ready", ChatFormatting.GREEN, pokemonName(pokemon)));
    }

    private static void onEvolutionComplete(EvolutionCompleteEvent event) {
        CobblemonChatAlertsConfig.Config config = CobblemonChatAlertsConfig.get();
        if (!config.alertEvolutionComplete) {
            return;
        }

        Pokemon evolvedPokemon = event.getPokemon();
        Pokemon sourcePokemon = event.getSourcePokemon();

        sendToOwner(evolvedPokemon, alert(
                "evolution_complete",
                ChatFormatting.GREEN,
                pokemonName(sourcePokemon),
                pokemonName(evolvedPokemon)
        ));
    }

    private static void onPokedexDataChanged(PokedexDataChangedEvent.Post event) {
        CobblemonChatAlertsConfig.Config config = CobblemonChatAlertsConfig.get();
        if (!config.alertPokedex) {
            return;
        }

        ServerPlayer player = playerById(event.getPlayerUUID());
        if (player == null) {
            return;
        }

        String key = event.getKnowledge() == PokedexEntryProgress.CAUGHT ? "pokedex_caught" : "pokedex_seen";
        send(player, alert(key, ChatFormatting.GREEN, speciesName(event.getDataSource().getApparentSpecies())));
    }

    private static void sendToOwner(Pokemon pokemon, Component message) {
        ServerPlayer owner = pokemon.getOwnerPlayer();
        if (owner == null) {
            UUID ownerId = pokemon.getOwnerUUID();
            owner = ownerId == null ? null : playerById(ownerId);
        }

        if (owner != null) {
            send(owner, message);
        }
    }

    private static void sendToSpawnAudience(ServerLevel level, Entity entity, Component message, CobblemonChatAlertsConfig.Config config) {
        switch (config.rareSpawnAudience) {
            case GLOBAL -> {
                for (ServerPlayer player : level.players()) {
                    send(player, message);
                }
            }
            case CHUNK_RADIUS -> {
                ChunkPos spawnChunk = entity.chunkPosition();
                for (ServerPlayer player : level.players()) {
                    if (isWithinChunkRadius(player.chunkPosition(), spawnChunk, config.rareSpawnRadiusChunks)) {
                        send(player, message);
                    }
                }
            }
            case BLOCK_RADIUS -> {
                double maxDistance = (double) config.rareSpawnRadiusBlocks * (double) config.rareSpawnRadiusBlocks;
                for (ServerPlayer player : level.players()) {
                    if (player.distanceToSqr(entity) <= maxDistance) {
                        send(player, message);
                    }
                }
            }
        }
    }

    private static boolean isWithinChunkRadius(ChunkPos playerChunk, ChunkPos spawnChunk, int radius) {
        return Math.abs(playerChunk.x - spawnChunk.x) <= radius && Math.abs(playerChunk.z - spawnChunk.z) <= radius;
    }

    private static void send(ServerPlayer player, Component message) {
        player.sendSystemMessage(message);
    }

    private static ServerPlayer playerById(UUID playerId) {
        MinecraftServer server = currentServer;
        if (server == null || playerId == null) {
            return null;
        }
        return server.getPlayerList().getPlayer(playerId);
    }

    private static List<Component> rareReasons(Pokemon pokemon, CobblemonChatAlertsConfig.Config config) {
        List<Component> reasons = new ArrayList<>();
        if (pokemon.getShiny() && config.rareFilterShiny) {
            reasons.add(reason("shiny"));
        }

        Species species = pokemon.getSpecies();
        if (species != null) {
            Set<String> labels = species.getLabels();
            if (labels != null && !labels.isEmpty()) {
                Set<String> normalizedLabels = new LinkedHashSet<>();
                for (String label : labels) {
                    normalizedLabels.add(label.toLowerCase(Locale.ROOT));
                }

                Set<String> rareLabelKeys = new LinkedHashSet<>();
                for (String label : normalizedLabels) {
                    String key = labelKey(label);
                    if (RARE_LABELS.contains(label) && isRareLabelEnabled(key, config)) {
                        rareLabelKeys.add(key);
                    }
                }

                for (String key : rareLabelKeys) {
                    reasons.add(reason(key));
                }
            }
        }

        addIvReasons(pokemon, config, reasons);
        return reasons;
    }

    private static boolean isRareLabelEnabled(String key, CobblemonChatAlertsConfig.Config config) {
        return switch (key) {
            case "legendary" -> config.rareFilterLegendary;
            case "mythical" -> config.rareFilterMythical;
            case "restricted" -> config.rareFilterRestricted;
            case "sublegendary" -> config.rareFilterSublegendary;
            case "ultra_beast" -> config.rareFilterUltraBeast;
            default -> true;
        };
    }

    private static void addIvReasons(Pokemon pokemon, CobblemonChatAlertsConfig.Config config, List<Component> reasons) {
        if (!config.rareFilterPerfectIvs && !config.rareFilterHighIvs) {
            return;
        }

        int total = pokemon.getIvs().getEffectiveBattleTotal();
        if (config.rareFilterPerfectIvs && total == IVs.MAX_TOTAL) {
            reasons.add(reason("perfect_ivs"));
            return;
        }

        if (config.rareFilterHighIvs && isAtLeastIvPercent(total, config.highIvThresholdPercent)) {
            reasons.add(reason("high_ivs", config.highIvThresholdPercent));
        }
    }

    private static boolean isAtLeastIvPercent(int total, int thresholdPercent) {
        return total * 100 >= thresholdPercent * IVs.MAX_TOTAL;
    }

    private static Component pokemonName(Pokemon pokemon) {
        return pokemon.getDisplayName(false).withStyle(ChatFormatting.AQUA);
    }

    private static Component speciesName(Species species) {
        if (species == null) {
            return Component.translatable("label." + MOD_ID + ".pokemon").withStyle(ChatFormatting.AQUA);
        }
        return species.getTranslatedName().withStyle(ChatFormatting.AQUA);
    }

    private static String labelKey(String label) {
        return switch (label) {
            case "ultra_beast", "ultra-beast", "ultrabeast" -> "ultra_beast";
            default -> label;
        };
    }

    private static MutableComponent reason(String key, Object... args) {
        return Component.translatable("label." + MOD_ID + "." + key, args);
    }

    private static MutableComponent rareSpawnDetails(Pokemon pokemon, ServerLevel level, BlockPos pos, CobblemonChatAlertsConfig.Config config) {
        MutableComponent details = rarePokemonDetails(pokemon, config);
        if (config.showCoordinates) {
            details.append(detail("coordinates", coordinates(pos, config)));
        }
        if (config.showBiome) {
            details.append(detail("biome", biomeName(level, pos).withStyle(ChatFormatting.YELLOW)));
        }
        return details;
    }

    private static MutableComponent rarePokemonDetails(Pokemon pokemon, CobblemonChatAlertsConfig.Config config) {
        MutableComponent details = Component.empty();
        if (config.showLevel) {
            details.append(detail("level", Component.literal(Integer.toString(pokemon.getLevel())).withStyle(ChatFormatting.YELLOW)));
        }
        if (config.showIvs) {
            int total = pokemon.getIvs().getEffectiveBattleTotal();
            int percent = Math.round((float) total * 100.0F / (float) IVs.MAX_TOTAL);
            details.append(detail(
                    "ivs",
                    Component.literal(Integer.toString(total)).withStyle(ChatFormatting.YELLOW),
                    Component.literal(Integer.toString(IVs.MAX_TOTAL)).withStyle(ChatFormatting.YELLOW),
                    Component.literal(Integer.toString(percent)).withStyle(ChatFormatting.YELLOW)
            ));
        }
        return details;
    }

    private static MutableComponent coordinates(BlockPos pos, CobblemonChatAlertsConfig.Config config) {
        String value = pos.getX() + " " + pos.getY() + " " + pos.getZ();
        MutableComponent component = Component.literal(value).withStyle(ChatFormatting.YELLOW);
        if (!config.clickableCoordinates) {
            return component;
        }

        return component.withStyle(style -> style
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp " + value))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("message." + MOD_ID + ".teleport_hover"))));
    }

    private static MutableComponent biomeName(ServerLevel level, BlockPos pos) {
        Holder<Biome> biome = level.getBiome(pos);
        return biome.unwrapKey()
                .map(key -> {
                    String translationKey = "biome." + key.location().getNamespace() + "." + key.location().getPath();
                    return Component.translatable(translationKey);
                })
                .orElse(Component.translatable("label." + MOD_ID + ".unknown_biome"));
    }

    private static MutableComponent detail(String key, Object... args) {
        return Component.translatable("message." + MOD_ID + ".detail_" + key, args).withStyle(ChatFormatting.GRAY);
    }

    private static MutableComponent join(List<Component> components) {
        MutableComponent result = Component.empty();
        for (int i = 0; i < components.size(); i++) {
            if (i > 0) {
                result.append(Component.literal(", "));
            }
            result.append(components.get(i));
        }
        return result;
    }

    private static MutableComponent alert(String key, ChatFormatting style, Object... args) {
        return prefix().append(Component.translatable("message." + MOD_ID + "." + key, args).withStyle(style));
    }

    private static MutableComponent prefix() {
        return Component.literal("[Cobblemon] ").withStyle(ChatFormatting.DARK_AQUA);
    }
}
