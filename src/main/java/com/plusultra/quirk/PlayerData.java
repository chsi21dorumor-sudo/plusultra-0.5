package com.plusultra.quirk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.plusultra.core.ModAttachments;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.player.Player;

/** Mutable per-player state, stored as a NeoForge data attachment. */
public class PlayerData {
    /** Cooldown slots: 0-2 primary Quirk, 3-5 fused Quirk, 6 Fusion Strike. */
    public static final int CD_SLOTS = 7;
    public static final int FUSION_STRIKE = 6;

    public static final Codec<PlayerData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.optionalFieldOf("quirk", "").forGetter(d -> d.quirk),
            Codec.STRING.optionalFieldOf("fusion", "").forGetter(d -> d.fusion),
            Codec.BOOL.optionalFieldOf("active_fusion", false).forGetter(d -> d.useFusion),
            Codec.INT.optionalFieldOf("level", 1).forGetter(d -> d.level),
            Codec.INT.optionalFieldOf("xp", 0).forGetter(d -> d.xp),
            Codec.FLOAT.optionalFieldOf("stamina", 100f).forGetter(d -> d.stamina),
            Codec.LONG.listOf().optionalFieldOf("cooldowns", List.of(0L, 0L, 0L, 0L, 0L, 0L, 0L))
                    .forGetter(d -> longList(d.cd)),
            Codec.STRING.optionalFieldOf("mission", "").forGetter(d -> d.mission),
            Codec.INT.optionalFieldOf("mission_progress", 0).forGetter(d -> d.missionProgress),
            Codec.INT.optionalFieldOf("mission_goal", 0).forGetter(d -> d.missionGoal),
            Codec.INT.optionalFieldOf("points", 0).forGetter(d -> d.points),
            Codec.INT.optionalFieldOf("skill_points", 0).forGetter(d -> d.skillPoints),
            Codec.INT.listOf().optionalFieldOf("talents", List.of(0, 0, 0, 0, 0))
                    .forGetter(d -> intList(d.talents))
    ).apply(i, PlayerData::new));

    public String quirk = "";
    /** Second (fused) Quirk, empty if none. */
    public String fusion = "";
    /** True when the fused Quirk is the one currently on Z/X/C. */
    public boolean useFusion = false;
    public int level = 1;
    public int xp = 0;
    public float stamina = 100f;
    /** Game-time tick at which each cooldown slot is ready again. */
    public final long[] cd = new long[CD_SLOTS];
    public String mission = "";
    public int missionProgress = 0;
    public int missionGoal = 0;
    public int points = 0;
    public int skillPoints = 0;
    public final int[] talents = new int[Talent.values().length];

    /** Not saved: set every second from nearby agency members. */
    public double synergy = 1.0;
    /** Not saved: visual aura while a buff ability is active. */
    public long auraUntil = 0;
    public int auraColor = 0xFFFFFF;

    public PlayerData() {}

    public PlayerData(String quirk, String fusion, boolean useFusion, int level, int xp, float stamina,
                      List<Long> cooldowns, String mission, int missionProgress, int missionGoal,
                      int points, int skillPoints, List<Integer> talentRanks) {
        this.quirk = quirk;
        this.fusion = fusion;
        this.useFusion = useFusion;
        this.level = level;
        this.xp = xp;
        this.stamina = stamina;
        for (int k = 0; k < CD_SLOTS && k < cooldowns.size(); k++) this.cd[k] = cooldowns.get(k);
        this.mission = mission;
        this.missionProgress = missionProgress;
        this.missionGoal = missionGoal;
        this.points = points;
        this.skillPoints = skillPoints;
        for (int k = 0; k < talents.length && k < talentRanks.size(); k++) this.talents[k] = talentRanks.get(k);
    }

    private static List<Long> longList(long[] a) {
        List<Long> out = new ArrayList<>();
        for (long v : a) out.add(v);
        return out;
    }

    private static List<Integer> intList(int[] a) {
        List<Integer> out = new ArrayList<>();
        for (int v : a) out.add(v);
        return out;
    }

    public static PlayerData get(Player p) {
        return p.getData(ModAttachments.PLAYER_DATA);
    }

    public boolean hasQuirk() {
        return !quirk.isEmpty() && Quirk.byId(quirk) != null;
    }

    public boolean hasFusion() {
        return hasQuirk() && !fusion.isEmpty() && Quirk.byId(fusion) != null;
    }

    /** The Quirk currently bound to Z/X/C. */
    public Quirk activeQuirk() {
        if (useFusion && hasFusion()) return Quirk.byId(fusion);
        return Quirk.byId(quirk);
    }

    /** First cooldown index of the active Quirk. */
    public int cdBase() {
        return (useFusion && hasFusion()) ? 3 : 0;
    }

    public int talent(Talent t) {
        return talents[t.ordinal()];
    }

    public int spentPoints() {
        int sum = 0;
        for (int v : talents) sum += v;
        return sum;
    }

    public float maxStamina() {
        return 100f + 10f * (level - 1) + 10f * talent(Talent.ENDURANCE);
    }

    public int xpNeeded() {
        return 50 * level;
    }

    public double costMultiplier() {
        return 1.0 - 0.05 * talent(Talent.EFFICIENCY);
    }

    public double cooldownMultiplier() {
        return 1.0 - 0.05 * talent(Talent.FOCUS);
    }

    public double regenMultiplier() {
        return 1.0 + 0.10 * talent(Talent.RECOVERY);
    }

    public double damageMultiplier() {
        return 1.0 + 0.08 * talent(Talent.POWER);
    }

    /** Gives all spent talent points back. */
    public void respec() {
        skillPoints += spentPoints();
        for (int k = 0; k < talents.length; k++) talents[k] = 0;
        stamina = Math.min(stamina, maxStamina());
    }

    public void setQuirk(String id) {
        this.quirk = id;
        this.fusion = "";
        this.useFusion = false;
        this.level = 1;
        this.xp = 0;
        this.skillPoints = 0;
        for (int k = 0; k < talents.length; k++) talents[k] = 0;
        this.stamina = maxStamina();
        for (int k = 0; k < CD_SLOTS; k++) cd[k] = 0;
    }

    public void clearQuirk() {
        setQuirk("");
    }

    public void removeFusion() {
        this.fusion = "";
        this.useFusion = false;
        cd[3] = cd[4] = cd[5] = cd[FUSION_STRIKE] = 0;
    }
}
