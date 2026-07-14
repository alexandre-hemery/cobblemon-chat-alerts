package dev.alexandrehemery.cobblemonchatalerts;

import java.util.ArrayList;
import java.util.List;

final class RarityEvaluation {
    private RarityEvaluation() {
    }

    static List<Reason> evaluate(
            boolean shiny,
            boolean legendary,
            boolean mythical,
            boolean restricted,
            boolean ultraBeast,
            int ivTotal,
            int maxIvTotal,
            Filters filters
    ) {
        List<Reason> reasons = new ArrayList<>();
        if (shiny && filters.shiny()) {
            reasons.add(Reason.SHINY);
        }
        if (legendary && filters.legendary()) {
            reasons.add(Reason.LEGENDARY);
        }
        if (mythical && filters.mythical()) {
            reasons.add(Reason.MYTHICAL);
        }
        if (restricted && filters.restricted()) {
            reasons.add(Reason.RESTRICTED);
        }
        if (ultraBeast && filters.ultraBeast()) {
            reasons.add(Reason.ULTRA_BEAST);
        }

        if (filters.perfectIvs() && ivTotal == maxIvTotal) {
            reasons.add(Reason.PERFECT_IVS);
        } else if (filters.highIvs() && isAtLeastIvPercent(ivTotal, maxIvTotal, filters.highIvThresholdPercent())) {
            reasons.add(Reason.HIGH_IVS);
        }

        return reasons;
    }

    static boolean isAtLeastIvPercent(int ivTotal, int maxIvTotal, int thresholdPercent) {
        return ivTotal * 100 >= thresholdPercent * maxIvTotal;
    }

    enum Reason {
        SHINY("shiny"),
        LEGENDARY("legendary"),
        MYTHICAL("mythical"),
        RESTRICTED("restricted"),
        ULTRA_BEAST("ultra_beast"),
        PERFECT_IVS("perfect_ivs"),
        HIGH_IVS("high_ivs");

        private final String translationKey;

        Reason(String translationKey) {
            this.translationKey = translationKey;
        }

        String translationKey() {
            return translationKey;
        }
    }

    record Filters(
            boolean shiny,
            boolean legendary,
            boolean mythical,
            boolean restricted,
            boolean ultraBeast,
            boolean perfectIvs,
            boolean highIvs,
            int highIvThresholdPercent
    ) {
    }
}
