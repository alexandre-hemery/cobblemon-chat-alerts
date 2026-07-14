package dev.alexandrehemery.cobblemonchatalerts;

import com.cobblemon.mod.common.api.pokedex.PokedexEntryProgress;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PokedexProgressEvaluationTest {
    @Test
    void acceptsOnlyProgressUpgrades() {
        assertAll(
                () -> assertTrue(PokedexProgressEvaluation.isUpgrade(PokedexEntryProgress.NONE, PokedexEntryProgress.ENCOUNTERED)),
                () -> assertTrue(PokedexProgressEvaluation.isUpgrade(PokedexEntryProgress.NONE, PokedexEntryProgress.CAUGHT)),
                () -> assertTrue(PokedexProgressEvaluation.isUpgrade(PokedexEntryProgress.ENCOUNTERED, PokedexEntryProgress.CAUGHT)),
                () -> assertFalse(PokedexProgressEvaluation.isUpgrade(PokedexEntryProgress.NONE, PokedexEntryProgress.NONE)),
                () -> assertFalse(PokedexProgressEvaluation.isUpgrade(PokedexEntryProgress.ENCOUNTERED, PokedexEntryProgress.ENCOUNTERED)),
                () -> assertFalse(PokedexProgressEvaluation.isUpgrade(PokedexEntryProgress.CAUGHT, PokedexEntryProgress.CAUGHT)),
                () -> assertFalse(PokedexProgressEvaluation.isUpgrade(PokedexEntryProgress.ENCOUNTERED, PokedexEntryProgress.NONE)),
                () -> assertFalse(PokedexProgressEvaluation.isUpgrade(PokedexEntryProgress.CAUGHT, PokedexEntryProgress.ENCOUNTERED)),
                () -> assertFalse(PokedexProgressEvaluation.isUpgrade(PokedexEntryProgress.CAUGHT, PokedexEntryProgress.NONE))
        );
    }
}
