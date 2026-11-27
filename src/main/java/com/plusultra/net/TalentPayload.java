package com.plusultra.net;

import com.plusultra.PlusUltra;
import com.plusultra.quirk.AbilityManager;
import com.plusultra.quirk.PlayerData;
import com.plusultra.quirk.Talent;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Client -> server: spend a skill point on talent #action, or respec when action == RESPEC. */
public record TalentPayload(int action) implements CustomPacketPayload {
    public static final int RESPEC = 99;

    public static final Type<TalentPayload> TYPE = new Type<>(PlusUltra.id("talent"));
    public static final StreamCodec<ByteBuf, TalentPayload> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(TalentPayload::new, TalentPayload::action);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(TalentPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sp)) return;
            PlayerData d = PlayerData.get(sp);
            if (!d.hasQuirk()) return;
            Talent[] all = Talent.values();
            if (payload.action == RESPEC) {
                d.respec();
            } else if (payload.action >= 0 && payload.action < all.length) {
                Talent t = all[payload.action];
                if (d.skillPoints > 0 && d.talent(t) < t.maxRank) {
                    d.skillPoints--;
                    d.talents[t.ordinal()]++;
                }
            }
            AbilityManager.send(sp);
        });
    }
}
