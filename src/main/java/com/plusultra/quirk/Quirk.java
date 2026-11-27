package com.plusultra.quirk;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.RandomSource;

/** The Quirks a player can awaken. Slot 0/1 are basic abilities, slot 2 is the Plus Ultra move. */
public enum Quirk {
    ONE_FOR_ALL("one_for_all", 0x7CFC00,
            new QuirkAbility("smash", 20, 30, Abilities::smash),
            new QuirkAbility("delaware_leap", 15, 60, Abilities::delawareLeap),
            new QuirkAbility("full_cowl", 60, 1200, Abilities::fullCowl)),

    HALF_COLD_HALF_HOT("half_cold_half_hot", 0x7FDBFF,
            new QuirkAbility("flame_burst", 18, 40, Abilities::flameBurst),
            new QuirkAbility("ice_burst", 18, 40, Abilities::iceBurst),
            new QuirkAbility("flashfreeze_heatwave", 70, 1400, Abilities::flashfreezeHeatwave)),

    EXPLOSION("explosion", 0xFF8C1A,
            new QuirkAbility("blast", 15, 25, Abilities::blast),
            new QuirkAbility("blast_propulsion", 15, 50, Abilities::blastPropulsion),
            new QuirkAbility("howitzer_impact", 65, 1300, Abilities::howitzerImpact)),

    HARDENING("hardening", 0xC0392B,
            new QuirkAbility("harden", 20, 300, Abilities::harden),
            new QuirkAbility("red_riot_punch", 15, 30, Abilities::redRiotPunch),
            new QuirkAbility("unbreakable", 60, 1500, Abilities::unbreakable)),

    ZERO_GRAVITY("zero_gravity", 0xFFB6C1,
            new QuirkAbility("float", 20, 80, Abilities::floatTargets),
            new QuirkAbility("weightless", 15, 100, Abilities::weightless),
            new QuirkAbility("meteor_shower", 65, 1400, Abilities::meteorShower)),

    ENGINE("engine", 0x3498DB,
            new QuirkAbility("recipro_burst", 20, 200, Abilities::reciproBurst),
            new QuirkAbility("engine_kick", 15, 40, Abilities::engineKick),
            new QuirkAbility("recipro_extend", 60, 1300, Abilities::reciproExtend)),

    CREATION("creation", 0xE67E22,
            new QuirkAbility("forge_shield", 20, 300, MoreAbilities::forgeShield),
            new QuirkAbility("creation_cannon", 18, 50, MoreAbilities::creationCannon),
            new QuirkAbility("iron_guardians", 65, 1500, MoreAbilities::ironGuardians)),

    INVISIBILITY("invisibility", 0xB0D0E8,
            new QuirkAbility("vanish", 20, 260, MoreAbilities::vanish),
            new QuirkAbility("light_bend_strike", 18, 80, MoreAbilities::lightBendStrike),
            new QuirkAbility("mirage", 60, 1400, MoreAbilities::mirage)),

    FIERCE_WINGS("fierce_wings", 0xD9382A,
            new QuirkAbility("wind_gale", 15, 60, MoreAbilities::windGale),
            new QuirkAbility("feather_barrage", 20, 70, MoreAbilities::featherBarrage),
            new QuirkAbility("soar", 60, 1300, MoreAbilities::soar)),

    DARK_SHADOW("dark_shadow", 0x2C2C54,
            new QuirkAbility("shadow_jab", 15, 30, MoreAbilities::shadowJab),
            new QuirkAbility("shadow_veil", 20, 320, MoreAbilities::shadowVeil),
            new QuirkAbility("rampage", 65, 1500, MoreAbilities::rampage)),

    BLACK_WHIP("black_whip", 0x4A235A,
            new QuirkAbility("lash", 15, 35, MoreAbilities::lash),
            new QuirkAbility("tether_swing", 12, 50, MoreAbilities::tetherSwing),
            new QuirkAbility("constrict", 60, 1300, MoreAbilities::constrict)),

    ACID("acid", 0xA6E22E,
            new QuirkAbility("acid_spit", 15, 35, MoreAbilities::acidSpit),
            new QuirkAbility("acid_pool", 22, 200, MoreAbilities::acidPool),
            new QuirkAbility("acid_flood", 65, 1500, MoreAbilities::acidFlood)),

    EARPHONE_JACK("earphone_jack", 0xFF69B4,
            new QuirkAbility("heartbeat_pulse", 18, 45, MoreAbilities::heartbeatPulse),
            new QuirkAbility("sound_sense", 15, 400, MoreAbilities::soundSense),
            new QuirkAbility("heartbeat_fuzz", 65, 1400, MoreAbilities::heartbeatFuzz)),

    TAPE("tape", 0xD0D0D0,
            new QuirkAbility("tape_shot", 15, 40, MoreAbilities::tapeShot),
            new QuirkAbility("tape_swing", 12, 50, MoreAbilities::tapeSwing),
            new QuirkAbility("tape_cocoon", 60, 1400, MoreAbilities::tapeCocoon));

    private final String id;
    private final int color;
    private final QuirkAbility[] abilities;

    Quirk(String id, int color, QuirkAbility... abilities) {
        this.id = id;
        this.color = color;
        this.abilities = abilities;
    }

    public String id() { return id; }
    public int color() { return color; }
    public QuirkAbility[] abilities() { return abilities; }

    public static Quirk byId(String id) {
        for (Quirk q : values()) if (q.id.equals(id)) return q;
        return null;
    }

    public static Quirk random(RandomSource rng) {
        Quirk[] all = values();
        return all[rng.nextInt(all.length)];
    }

    public static List<String> ids() {
        List<String> out = new ArrayList<>();
        for (Quirk q : values()) out.add(q.id);
        return out;
    }
}
