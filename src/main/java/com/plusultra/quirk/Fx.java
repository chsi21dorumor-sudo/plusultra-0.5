package com.plusultra.quirk;

import com.plusultra.net.AnimPayload;
import java.util.Collection;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3f;

/** Server-side visual effects built from vanilla particles: rings, beams, spirals, spheres, auras. */
public final class Fx {
    private Fx() {}

    public static DustParticleOptions dust(int rgb, float scale) {
        return new DustParticleOptions(
                new Vector3f(((rgb >> 16) & 255) / 255f, ((rgb >> 8) & 255) / 255f, (rgb & 255) / 255f), scale);
    }

    /** Flat horizontal ring. */
    public static void ring(ServerLevel l, ParticleOptions o, Vec3 c, double r, int n) {
        for (int i = 0; i < n; i++) {
            double a = Math.PI * 2 * i / n;
            l.sendParticles(o, c.x + Math.cos(a) * r, c.y, c.z + Math.sin(a) * r, 1, 0, 0, 0, 0);
        }
    }

    /** Straight line of particles. */
    public static void beam(ServerLevel l, ParticleOptions o, Vec3 from, Vec3 to, double step) {
        Vec3 d = to.subtract(from);
        double len = d.length();
        if (len < 1e-3) return;
        Vec3 dir = d.scale(1.0 / len);
        for (double t = 0; t <= len; t += step) {
            Vec3 p = from.add(dir.scale(t));
            l.sendParticles(o, p.x, p.y, p.z, 1, 0.03, 0.03, 0.03, 0);
        }
    }

    /** Rising spiral around a vertical axis. */
    public static void spiral(ServerLevel l, ParticleOptions o, Vec3 base, double height, double radius,
                              double turns, int n) {
        for (int i = 0; i < n; i++) {
            double f = i / (double) n;
            double a = f * turns * Math.PI * 2;
            l.sendParticles(o, base.x + Math.cos(a) * radius, base.y + f * height,
                    base.z + Math.sin(a) * radius, 1, 0, 0, 0, 0);
        }
    }

    /** Hollow sphere of particles. */
    public static void sphere(ServerLevel l, ParticleOptions o, Vec3 c, double r, int n) {
        for (int i = 0; i < n; i++) {
            double y = 1 - 2 * (i + 0.5) / n;
            double rad = Math.sqrt(1 - y * y);
            double phi = i * 2.399963;
            l.sendParticles(o, c.x + Math.cos(phi) * rad * r, c.y + y * r, c.z + Math.sin(phi) * rad * r,
                    1, 0, 0, 0, 0);
        }
    }

    public static void burst(ServerLevel l, ParticleOptions o, Vec3 c, int count, double spread, double speed) {
        l.sendParticles(o, c.x, c.y, c.z, count, spread, spread, spread, speed);
    }

    /** Falling streak (count 0 makes the offsets a velocity). */
    public static void streak(ServerLevel l, ParticleOptions o, Vec3 from, double speed) {
        l.sendParticles(o, from.x, from.y, from.z, 0, 0, -1, 0, speed);
    }

    /** Harmless lightning for show. */
    public static void lightning(ServerLevel l, Vec3 at) {
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(l);
        if (bolt == null) return;
        bolt.moveTo(at.x, at.y, at.z);
        bolt.setVisualOnly(true);
        l.addFreshEntity(bolt);
    }

    /** One tick of the glow around a buffed player. */
    public static void aura(ServerPlayer p, int rgb) {
        ServerLevel l = p.serverLevel();
        l.sendParticles(dust(rgb, 1.2f), p.getX(), p.getY() + 0.9, p.getZ(), 4, 0.35, 0.8, 0.35, 0);
        if (p.getRandom().nextInt(3) == 0) {
            l.sendParticles(ParticleTypes.END_ROD, p.getX(), p.getY() + 1, p.getZ(), 1, 0.3, 0.6, 0.3, 0.02);
        }
    }

    /** Which client aura style a Quirk uses (see AnimsCore.Aura). */
    public static int auraStyle(Quirk q) {
        if (q == null) return 0;
        return switch (q) {
            case ONE_FOR_ALL, ENGINE -> 0;
            case EXPLOSION -> 1;
            case HARDENING -> 2;
            case ZERO_GRAVITY -> 3;
            case DARK_SHADOW -> 4;
            case FIERCE_WINGS -> 5;
            case CREATION -> 6;
            case INVISIBILITY -> 7;
            case ACID -> 8;
            case EARPHONE_JACK -> 9;
            case HALF_COLD_HALF_HOT -> 10;
            case TAPE -> 11;
            case BLACK_WHIP -> 12;
        };
    }

    /** Starts the client-side aura animation for the player's active Quirk. */
    public static void startAura(ServerPlayer p, int ticks) {
        PlayerData d = PlayerData.get(p);
        Quirk q = d.activeQuirk();
        d.auraUntil = p.level().getGameTime() + ticks;
        d.auraColor = q == null ? 0xFFFFFF : q.color();
        play(FxType.AURA).src(p).rgb(d.auraColor).n(ticks).m(auraStyle(q)).send(p);
    }

    // ------------------------------------------------------------------ client animations

    /** Begin building an animation packet. Finish with one of the send methods. */
    public static Call play(FxType type) {
        return new Call(type);
    }

    public static final class Call {
        private final FxType type;
        private int src = -1;
        private int[] targets = new int[0];
        private Vec3 a = Vec3.ZERO;
        private Vec3 b = Vec3.ZERO;
        private int rgb = 0xFFFFFF;
        private int n = 0;
        private int m = 0;
        private float f = 0f;

        private Call(FxType type) {
            this.type = type;
        }

        public Call src(Entity e) { this.src = e.getId(); return this; }

        public Call targets(Collection<? extends Entity> list) {
            int k = Math.min(list.size(), 16);
            int[] ids = new int[k];
            int i = 0;
            for (Entity e : list) {
                if (i >= k) break;
                ids[i++] = e.getId();
            }
            this.targets = ids;
            return this;
        }

        public Call a(Vec3 v) { this.a = v; return this; }
        public Call b(Vec3 v) { this.b = v; return this; }
        public Call rgb(int v) { this.rgb = v; return this; }
        public Call n(int v) { this.n = v; return this; }
        public Call m(int v) { this.m = v; return this; }
        public Call f(float v) { this.f = v; return this; }

        /** Sends to every player within 64 blocks of {@code near}. */
        public void send(ServerLevel level, Vec3 near) {
            AnimPayload payload = new AnimPayload(type.ordinal(), src, targets, a.x, a.y, a.z, b.x, b.y, b.z,
                    rgb, n, m, f);
            for (ServerPlayer sp : level.players()) {
                if (sp.distanceToSqr(near.x, near.y, near.z) < 64.0 * 64.0) {
                    PacketDistributor.sendToPlayer(sp, payload);
                }
            }
        }

        /** Sends to everyone near the given entity. */
        public void send(Entity near) {
            if (near.level() instanceof ServerLevel sl) send(sl, near.position());
        }
    }

    public static void levelUp(ServerPlayer p) {
        ServerLevel l = p.serverLevel();
        Vec3 c = p.position();
        ring(l, dust(0xFFD700, 1.3f), c.add(0, 0.1, 0), 1.2, 20);
        ring(l, dust(0xFFD700, 1.3f), c.add(0, 1.0, 0), 1.6, 24);
        spiral(l, ParticleTypes.END_ROD, c, 2.2, 0.8, 2, 30);
        l.playSound(null, c.x, c.y, c.z, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1f, 1.2f);
    }
}
