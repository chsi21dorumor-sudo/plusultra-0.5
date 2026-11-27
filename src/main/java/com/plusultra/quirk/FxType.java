package com.plusultra.quirk;

/** Every client-side animation the server can trigger. The ordinal is what goes over the wire. */
public enum FxType {
    /** Continuous glow around an entity. m = style, n = ticks, rgb = color. */
    AURA,
    /** Jagged lightning radiating from a point. a = center, f = radius, n = duration. */
    LIGHTNING_BURST,
    /** Punch impact with arm lightning, air blast and ground cracks. src, a = impact, b = direction, f = intensity. */
    SMASH,
    /** Ground burst and lightning trailing a jumping entity. */
    LEAP,
    /** Fire (m = 0) or wind (m = 1) stream from the hand. src, f = range. */
    FLAME_JET,
    /** Ice spikes growing along the ground. a = start, b = flat direction, f = range. */
    ICE_WAVE,
    /** Half fire / half ice nova. a = center, f = radius. */
    DUAL_NOVA,
    /** Staged MHA-style explosion. a = center, f = radius. */
    MHA_EXPLOSION,
    /** Sparks popping in the palms. src, n = ticks. */
    PALM_CRACKLE,
    /** Explosions behind a rocketing entity. src, b = direction, n = ticks. */
    BLAST_JET,
    /** Spinning fire spiral, then a big explosion at a. src, a = target, n = windup ticks, f = radius. */
    HOWITZER_WINDUP,
    /** Shards flying out of a point. a = center, f = scale. */
    SHARDS,
    /** Pink gravity bubble with floating targets. a = center, f = radius, targets. */
    GRAVITY_FIELD,
    /** Meteors raining down. a = center, f = radius. */
    METEORS,
    /** Speed lines and exhaust behind a fast entity. src, n = ticks, m = 1 for lightning. */
    SPEED_TRAIL,
    /** Expanding ring on the ground. a = center, f = radius, n = ticks. */
    RING_BURST,
    /** Particles converging on a point. a = center, f = radius, n = ticks. */
    GATHER,
    /** Traveling projectile. a = from, b = to, m = style (0 bolt, 1 acid, 2 light streak). */
    PROJECTILE,
    /** Twinkling shimmer around an entity. src, n = ticks. */
    SHIMMER,
    /** Dome of twinkling light. a = center, f = radius, n = ticks. */
    DOME,
    /** Flapping wings behind an entity. src, n = ticks, m = 1 for soaring. */
    WINGS,
    /** Feathers flying to targets. src, targets, b = fallback direction, n = count. */
    FEATHER_VOLLEY,
    /** Shadow tendril lunge. src, a = end point, n = ticks. */
    SHADOW_ARM,
    /** Shadow tendrils erupting from the ground. a = center, f = radius. */
    SHADOW_ERUPT,
    /** Tape or whip strand. src, targets or a = anchor, n = hold ticks, m = style. */
    STRAND,
    /** Bubbling acid puddle. a = center, f = radius, n = ticks, m = 1 for flood. */
    ACID_POOL,
    /** Sound rings traveling forward. src, b = direction, f = range. */
    SOUND_WAVE,
    /** Sound ripples on the ground. a = center, f = radius, n = pulses, m = 1 for bass drop. */
    RIPPLE;

    public static FxType byId(int id) {
        FxType[] all = values();
        return id >= 0 && id < all.length ? all[id] : null;
    }
}
