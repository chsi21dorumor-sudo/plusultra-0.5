package com.plusultra.net;

import com.plusultra.PlusUltra;
import com.plusultra.client.ClientData;
import com.plusultra.quirk.PlayerData;
import com.plusultra.quirk.Quirk;
import com.plusultra.quirk.Talent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server -> client: everything the HUD and talent screen need.
 * quirk = the ACTIVE Quirk id, other = the Quirk you would swap to (empty if not fused).
 * Cooldowns are sent as ticks remaining.
 */
public record SyncPayload(String quirk, String other, int level, int xp, int xpNeeded, float stamina,
                          float maxStamina, int cd0, int cd1, int cd2, int cdFusion, int points, String mission,
                          int missionProgress, int missionGoal, int skillPoints, int[] talents)
        implements CustomPacketPayload {

    public static final Type<SyncPayload> TYPE = new Type<>(PlusUltra.id("sync"));
    public static final StreamCodec<FriendlyByteBuf, SyncPayload> STREAM_CODEC =
            StreamCodec.of(SyncPayload::write, SyncPayload::read);

    private static int rem(long end, long now) {
        return (int) Math.max(0, end - now);
    }

    public static SyncPayload from(ServerPlayer p, PlayerData d) {
        long now = p.level().getGameTime();
        Quirk active = d.activeQuirk();
        int b = d.cdBase();
        String other = "";
        if (d.hasFusion()) other = d.useFusion ? d.quirk : d.fusion;
        return new SyncPayload(active == null ? "" : active.id(), other, d.level, d.xp, d.xpNeeded(), d.stamina,
                d.maxStamina(), rem(d.cd[b], now), rem(d.cd[b + 1], now), rem(d.cd[b + 2], now),
                rem(d.cd[PlayerData.FUSION_STRIKE], now), d.points, d.mission, d.missionProgress, d.missionGoal,
                d.skillPoints, d.talents.clone());
    }

    private static void write(FriendlyByteBuf buf, SyncPayload p) {
        buf.writeUtf(p.quirk);
        buf.writeUtf(p.other);
        buf.writeVarInt(p.level);
        buf.writeVarInt(p.xp);
        buf.writeVarInt(p.xpNeeded);
        buf.writeFloat(p.stamina);
        buf.writeFloat(p.maxStamina);
        buf.writeVarInt(p.cd0);
        buf.writeVarInt(p.cd1);
        buf.writeVarInt(p.cd2);
        buf.writeVarInt(p.cdFusion);
        buf.writeVarInt(p.points);
        buf.writeUtf(p.mission);
        buf.writeVarInt(p.missionProgress);
        buf.writeVarInt(p.missionGoal);
        buf.writeVarInt(p.skillPoints);
        for (int i = 0; i < Talent.values().length; i++) buf.writeVarInt(p.talents[i]);
    }

    private static SyncPayload read(FriendlyByteBuf buf) {
        String quirk = buf.readUtf();
        String other = buf.readUtf();
        int level = buf.readVarInt();
        int xp = buf.readVarInt();
        int xpNeeded = buf.readVarInt();
        float stamina = buf.readFloat();
        float maxStamina = buf.readFloat();
        int cd0 = buf.readVarInt();
        int cd1 = buf.readVarInt();
        int cd2 = buf.readVarInt();
        int cdFusion = buf.readVarInt();
        int points = buf.readVarInt();
        String mission = buf.readUtf();
        int missionProgress = buf.readVarInt();
        int missionGoal = buf.readVarInt();
        int skillPoints = buf.readVarInt();
        int[] talents = new int[Talent.values().length];
        for (int i = 0; i < talents.length; i++) talents[i] = buf.readVarInt();
        return new SyncPayload(quirk, other, level, xp, xpNeeded, stamina, maxStamina, cd0, cd1, cd2, cdFusion,
                points, mission, missionProgress, missionGoal, skillPoints, talents);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> ClientData.update(payload));
    }
}
