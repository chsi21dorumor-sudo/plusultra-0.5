package com.plusultra.client;

import com.plusultra.net.SyncPayload;
import com.plusultra.quirk.Talent;

/** Client-side mirror of the server's player data. Plain fields only, safe to load anywhere. */
public final class ClientData {
    /** The ACTIVE Quirk id (primary or fused), empty if the player has none. */
    public static String quirk = "";
    /** The Quirk you would swap to, empty if not fused. */
    public static String other = "";
    public static int level = 1;
    public static int xp = 0;
    public static int xpNeeded = 50;
    public static float stamina = 100f;
    public static float maxStamina = 100f;
    public static int points = 0;
    public static String mission = "";
    public static int missionProgress = 0;
    public static int missionGoal = 0;
    public static int skillPoints = 0;
    public static final int[] talents = new int[Talent.values().length];

    /** Incremented every client tick; cooldowns are stored as the tick they end on. */
    public static long tick = 0;
    /** 0-2 = abilities of the active Quirk, 3 = Fusion Strike. */
    public static final long[] cdEnd = new long[4];

    private ClientData() {}

    public static void update(SyncPayload p) {
        quirk = p.quirk();
        other = p.other();
        level = p.level();
        xp = p.xp();
        xpNeeded = p.xpNeeded();
        stamina = p.stamina();
        maxStamina = p.maxStamina();
        points = p.points();
        mission = p.mission();
        missionProgress = p.missionProgress();
        missionGoal = p.missionGoal();
        skillPoints = p.skillPoints();
        System.arraycopy(p.talents(), 0, talents, 0, Math.min(p.talents().length, talents.length));
        cdEnd[0] = tick + p.cd0();
        cdEnd[1] = tick + p.cd1();
        cdEnd[2] = tick + p.cd2();
        cdEnd[3] = tick + p.cdFusion();
    }

    public static boolean hasQuirk() {
        return !quirk.isEmpty();
    }

    public static int cooldownTicks(int slot) {
        return (int) Math.max(0, cdEnd[slot] - tick);
    }
}
