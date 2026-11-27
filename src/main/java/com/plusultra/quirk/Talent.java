package com.plusultra.quirk;

/** Talent tree: every level-up grants one skill point to spend on these. */
public enum Talent {
    POWER("power", 5),        // +8% Quirk damage per rank
    ENDURANCE("endurance", 5), // +10 max stamina per rank
    RECOVERY("recovery", 5),  // +10% stamina regen per rank
    FOCUS("focus", 5),        // -5% cooldowns per rank
    EFFICIENCY("efficiency", 4); // -5% stamina cost per rank

    public final String id;
    public final int maxRank;

    Talent(String id, int maxRank) {
        this.id = id;
        this.maxRank = maxRank;
    }
}
