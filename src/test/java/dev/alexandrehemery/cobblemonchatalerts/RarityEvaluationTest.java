package dev.alexandrehemery.cobblemonchatalerts;

import org.junit.jupiter.api.Test;

import java.util.List;

import static dev.alexandrehemery.cobblemonchatalerts.RarityEvaluation.Reason.HIGH_IVS;
import static dev.alexandrehemery.cobblemonchatalerts.RarityEvaluation.Reason.LEGENDARY;
import static dev.alexandrehemery.cobblemonchatalerts.RarityEvaluation.Reason.MYTHICAL;
import static dev.alexandrehemery.cobblemonchatalerts.RarityEvaluation.Reason.PERFECT_IVS;
import static dev.alexandrehemery.cobblemonchatalerts.RarityEvaluation.Reason.RESTRICTED;
import static dev.alexandrehemery.cobblemonchatalerts.RarityEvaluation.Reason.SHINY;
import static dev.alexandrehemery.cobblemonchatalerts.RarityEvaluation.Reason.ULTRA_BEAST;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RarityEvaluationTest {
    private static final int MAX_IV_TOTAL = 186;

    @Test
    void returnsEnabledReasonsInDisplayOrder() {
        RarityEvaluation.Filters filters = new RarityEvaluation.Filters(true, true, true, true, true, false, true, 90);

        List<RarityEvaluation.Reason> reasons = RarityEvaluation.evaluate(
                true, true, true, true, true, 180, MAX_IV_TOTAL, filters
        );

        assertEquals(List.of(SHINY, LEGENDARY, MYTHICAL, RESTRICTED, ULTRA_BEAST, HIGH_IVS), reasons);
    }

    @Test
    void ignoresCategoriesWhoseFiltersAreDisabled() {
        RarityEvaluation.Filters filters = new RarityEvaluation.Filters(false, false, false, false, false, false, false, 90);

        List<RarityEvaluation.Reason> reasons = RarityEvaluation.evaluate(
                true, true, true, true, true, MAX_IV_TOTAL, MAX_IV_TOTAL, filters
        );

        assertEquals(List.of(), reasons);
    }

    @Test
    void perfectIvsTakePrecedenceOverTheHighIvReason() {
        RarityEvaluation.Filters filters = new RarityEvaluation.Filters(false, false, false, false, false, true, true, 90);

        List<RarityEvaluation.Reason> reasons = RarityEvaluation.evaluate(
                false, false, false, false, false, MAX_IV_TOTAL, MAX_IV_TOTAL, filters
        );

        assertEquals(List.of(PERFECT_IVS), reasons);
    }

    @Test
    void perfectIvsCanStillMatchTheHighIvFilter() {
        RarityEvaluation.Filters filters = new RarityEvaluation.Filters(false, false, false, false, false, false, true, 90);

        List<RarityEvaluation.Reason> reasons = RarityEvaluation.evaluate(
                false, false, false, false, false, MAX_IV_TOTAL, MAX_IV_TOTAL, filters
        );

        assertEquals(List.of(HIGH_IVS), reasons);
    }

    @Test
    void highIvThresholdIsInclusive() {
        assertTrue(RarityEvaluation.isAtLeastIvPercent(168, MAX_IV_TOTAL, 90));
        assertFalse(RarityEvaluation.isAtLeastIvPercent(167, MAX_IV_TOTAL, 90));
    }
}
