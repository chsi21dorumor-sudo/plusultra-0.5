package com.plusultra.net;

import com.plusultra.PlusUltra;
import com.plusultra.client.anim.ClientAnims;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server -> client: play one animation. The meaning of each field depends on the animation type,
 * see {@link com.plusultra.quirk.FxType}.
 */
public record AnimPayload(int kind, int src, int[] targets, double ax, double ay, double az,
                          double bx, double by, double bz, int rgb, int n, int m, float f)
        implements CustomPacketPayload {

    public static final Type<AnimPayload> TYPE = new Type<>(PlusUltra.id("anim"));
    public static final StreamCodec<FriendlyByteBuf, AnimPayload> STREAM_CODEC =
            StreamCodec.of(AnimPayload::write, AnimPayload::read);

    private static void write(FriendlyByteBuf buf, AnimPayload p) {
        buf.writeVarInt(p.kind);
        buf.writeInt(p.src);
        buf.writeVarInt(p.targets.length);
        for (int id : p.targets) buf.writeInt(id);
        buf.writeDouble(p.ax);
        buf.writeDouble(p.ay);
        buf.writeDouble(p.az);
        buf.writeDouble(p.bx);
        buf.writeDouble(p.by);
        buf.writeDouble(p.bz);
        buf.writeInt(p.rgb);
        buf.writeVarInt(p.n);
        buf.writeVarInt(p.m);
        buf.writeFloat(p.f);
    }

    private static AnimPayload read(FriendlyByteBuf buf) {
        int kind = buf.readVarInt();
        int src = buf.readInt();
        int count = Math.min(buf.readVarInt(), 32);
        int[] targets = new int[count];
        for (int i = 0; i < count; i++) targets[i] = buf.readInt();
        return new AnimPayload(kind, src, targets,
                buf.readDouble(), buf.readDouble(), buf.readDouble(),
                buf.readDouble(), buf.readDouble(), buf.readDouble(),
                buf.readInt(), buf.readVarInt(), buf.readVarInt(), buf.readFloat());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AnimPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> ClientAnims.start(payload));
    }
}
