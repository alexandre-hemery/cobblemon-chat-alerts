package dev.alexandrehemery.cobblemonchatalerts;

import com.cobblemon.mod.common.api.pokedex.PokedexEntryProgress;

final class PokedexProgressEvaluation {
    private PokedexProgressEvaluation() {
    }

    static boolean isUpgrade(PokedexEntryProgress previous, PokedexEntryProgress next) {
        return next.ordinal() > previous.ordinal();
    }
}
