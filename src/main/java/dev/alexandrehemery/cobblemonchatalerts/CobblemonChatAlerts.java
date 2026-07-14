package dev.alexandrehemery.cobblemonchatalerts;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.ActorType;
import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.battles.BattleFaintedEvent;
import com.cobblemon.mod.common.api.events.entity.SpawnEvent;
import com.cobblemon.mod.common.api.events.pokemon.LevelUpEvent;
import com.cobblemon.mod.common.api.events.pokemon.PokedexDataChangedEvent;
import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent;
import com.cobblemon.mod.common.api.events.pokemon.evolution.EvolutionCompleteEvent;
import com.cobblemon.mod.common.api.events.pokemon.evolution.EvolutionTestedEvent;
import com.cobblemon.mod.common.api.pokedex.PokedexEntryProgress;
import com.cobblemon.mod.common.api.pokedex.SpeciesDexRecord;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.IVs;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
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
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CobblemonChatAlerts implements ModInitializer {
    public static final String MOD_ID = "cobblemon_chat_alerts";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final long EVOLUTION_READY_COOLDOWN_MS = 60_000L;
    private static final Map<UUID, Long> EVOLUTION_READY_NOTIFIED_AT = new ConcurrentHashMap<>();
    private static final Queue<PokemonEntity> PENDING_SPAWN_ALERTS = new ConcurrentLinkedQueue<>();
    private static final Set<PokedexProgressChange> PENDING_POKEDEX_PROGRESS = ConcurrentHashMap.newKeySet();

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
            PENDING_SPAWN_ALERTS.clear();
            PENDING_POKEDEX_PROGRESS.clear();
        });
        ServerTickEvents.END_SERVER_TICK.register(CobblemonChatAlerts::flushPendingSpawnAlerts);

        CobblemonEvents.POKEMON_ENTITY_SPAWN.subscribe(Priority.LOWEST, CobblemonChatAlerts::onPokemonSpawn);
        CobblemonEvents.POKEMON_CAPTURED.subscribe(CobblemonChatAlerts::onPokemonCaptured);
        CobblemonEvents.BATTLE_FAINTED.subscribe(CobblemonChatAlerts::onBattleFainted);
        CobblemonEvents.LEVEL_UP_EVENT.subscribe(Priority.LOWEST, CobblemonChatAlerts::onLevelUp);
        CobblemonEvents.EVOLUTION_TESTED.subscribe(Priority.LOWEST, CobblemonChatAlerts::onEvolutionTested);
        CobblemonEvents.EVOLUTION_COMPLETE.subscribe(CobblemonChatAlerts::onEvolutionComplete);
        CobblemonEvents.POKEDEX_DATA_CHANGED_PRE.subscribe(Priority.LOWEST, CobblemonChatAlerts::onPokedexDataChanging);
        CobblemonEvents.POKEDEX_DATA_CHANGED_POST.subscribe(Priority.LOWEST, CobblemonChatAlerts::onPokedexDataChanged);
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

        PENDING_SPAWN_ALERTS.add(entity);
    }

    private static void flushPendingSpawnAlerts(MinecraftServer server) {
        if (server != currentServer) {
            PENDING_SPAWN_ALERTS.clear();
            return;
        }

        PokemonEntity entity;
        while ((entity = PENDING_SPAWN_ALERTS.poll()) != null) {
            Level level = entity.level();
            if (entity.isRemoved() || !(level instanceof ServerLevel serverLevel)) {
                continue;
            }
            if (serverLevel.getEntity(entity.getUUID()) != entity) {
                continue;
            }

            sendRareSpawnAlert(entity, serverLevel);
        }
    }

    private static void sendRareSpawnAlert(PokemonEntity entity, ServerLevel serverLevel) {
        CobblemonChatAlertsConfig.Config config = CobblemonChatAlertsConfig.get();
        Pokemon pokemon = entity.getPokemon();
        if (!config.alertRareSpawns || pokemon.getOwnerUUID() != null) {
            return;
        }

        List<Component> rareReasons = rareReasons(pokemon, config);
        if (rareReasons.isEmpty()) {
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
        EVOLUTION_READY_NOTIFIED_AT.entrySet().removeIf(entry -> now - entry.getValue() >= EVOLUTION_READY_COOLDOWN_MS);
        if (EVOLUTION_READY_NOTIFIED_AT.putIfAbsent(uuid, now) != null) {
            return;
        }

        sendToOwner(pokemon, alert("evolution_ready", ChatFormatting.GREEN, pokemonName(pokemon)));
    }

    private static void onEvolutionComplete(EvolutionCompleteEvent event) {
        Pokemon evolvedPokemon = event.getPokemon();
        EVOLUTION_READY_NOTIFIED_AT.remove(evolvedPokemon.getUuid());

        CobblemonChatAlertsConfig.Config config = CobblemonChatAlertsConfig.get();
        if (!config.alertEvolutionComplete) {
            return;
        }

        Pokemon sourcePokemon = event.getSourcePokemon();

        sendToOwner(evolvedPokemon, alert(
                "evolution_complete",
                ChatFormatting.GREEN,
                pokemonName(sourcePokemon),
                pokemonName(evolvedPokemon)
        ));
    }

    private static void onPokedexDataChanging(PokedexDataChangedEvent.Pre event) {
        CobblemonChatAlertsConfig.Config config = CobblemonChatAlertsConfig.get();
        if (!config.alertPokedex) {
            return;
        }

        SpeciesDexRecord speciesRecord = event.getRecord().getSpeciesDexRecord();
        if (!PokedexProgressEvaluation.isUpgrade(speciesRecord.getKnowledge(), event.getKnowledge())) {
            return;
        }

        PENDING_POKEDEX_PROGRESS.add(new PokedexProgressChange(event.getPlayerUUID(), speciesRecord, event.getKnowledge()));
    }

    private static void onPokedexDataChanged(PokedexDataChangedEvent.Post event) {
        SpeciesDexRecord speciesRecord = event.getRecord().getSpeciesDexRecord();
        PokedexProgressChange progressChange = new PokedexProgressChange(event.getPlayerUUID(), speciesRecord, event.getKnowledge());
        if (!PENDING_POKEDEX_PROGRESS.remove(progressChange)) {
            return;
        }

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
        int ivTotal = pokemon.getIvs().getEffectiveBattleTotal();
        RarityEvaluation.Filters filters = new RarityEvaluation.Filters(
                config.rareFilterShiny,
                config.rareFilterLegendary,
                config.rareFilterMythical,
                config.rareFilterRestricted,
                config.rareFilterUltraBeast,
                config.rareFilterPerfectIvs,
                config.rareFilterHighIvs,
                config.highIvThresholdPercent
        );
        List<RarityEvaluation.Reason> evaluatedReasons = RarityEvaluation.evaluate(
                pokemon.getShiny(),
                pokemon.isLegendary(),
                pokemon.isMythical(),
                pokemon.hasLabels("restricted"),
                pokemon.isUltraBeast(),
                ivTotal,
                IVs.MAX_TOTAL,
                filters
        );

        List<Component> reasons = new ArrayList<>();
        for (RarityEvaluation.Reason evaluatedReason : evaluatedReasons) {
            if (evaluatedReason == RarityEvaluation.Reason.HIGH_IVS) {
                reasons.add(reason(evaluatedReason.translationKey(), config.highIvThresholdPercent));
            } else {
                reasons.add(reason(evaluatedReason.translationKey()));
            }
        }

        return reasons;
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

    private record PokedexProgressChange(UUID playerId, SpeciesDexRecord speciesRecord, PokedexEntryProgress knowledge) {
    }
}
