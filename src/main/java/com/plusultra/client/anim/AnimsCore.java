package com.plusultra.client.anim;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Auras, lightning, punches, leaps, projectiles and other general-purpose animations. */
public final class AnimsCore {
    private AnimsCore() {}

    private static BlockState ground(ClientLevel level, Vec3 at) {
        BlockState s = level.getBlockState(BlockPos.containing(at.x, at.y - 0.5, at.z));
        return s.isAir() ? Blocks.DIRT.defaultBlockState() : s;
    }

    // ================================================================== AURA

    /** Continuous glow around an entity. Style depends on the Quirk (see Fx.auraStyle). */
    public static final class Aura extends Anim {
        private final int src, rgb, ticks, style;

        public Aura(ClientLevel level, int src, int rgb, int ticks, int style) {
            super(level);
            this.src = src;
            this.rgb = rgb;
            this.ticks = ticks;
            this.style = style;
        }

        @Override
        protected boolean step() {
            Entity e = ent(src);
            if (e == null || e.isRemoved() || t >= ticks) return true;
            double h = e.getBbHeight();
            Vec3 base = e.position();
            switch (style) {
                case 0 -> { // lightning (One For All, Engine)
                    if (t % 2 == 0) {
                        for (int i = 0; i < 2; i++) {
                            Vec3 a = body(e);
                            P.bolt(level, a, a.add(P.unit().scale(0.55 + P.R.nextDouble() * 0.5)), rgb, 2, 0.12);
                        }
                    }
                    P.spark(level, body(e), P.rv(0.1).add(0, 0.04, 0), rgb, 0.12f, 9);
                    if (t % 3 == 0) P.vanilla(level, ParticleTypes.ELECTRIC_SPARK, body(e), P.rv(0.2));
                    if (t % 5 == 0) P.glow(level, base.add(P.rs() * 0.4, 0.1, P.rs() * 0.4), new Vec3(0, 0.05, 0), rgb, 0.3f, 8);
                }
                case 1 -> { // sparks (Explosion)
                    P.spark(level, body(e), P.rv(0.15).add(0, 0.06, 0), P.R.nextBoolean() ? rgb : 0xFFE08A, 0.1f, 10);
                    P.glow(level, body(e), Vec3.ZERO, rgb, 0.2f, 6);
                    if (t % 2 == 0) P.vanilla(level, ParticleTypes.FLAME, hand(e), P.rv(0.03));
                    if (t % 6 == 0) P.vanilla(level, ParticleTypes.SMOKE, body(e), new Vec3(0, 0.04, 0));
                }
                case 2 -> { // steel (Hardening)
                    P.vanilla(level, ParticleTypes.CRIT, body(e), P.rv(0.1));
                    P.glow(level, body(e), Vec3.ZERO, 0x8B0000, 0.26f, 6);
                    if (t % 3 == 0) P.spark(level, body(e), P.rv(0.08), 0xCCCCCC, 0.1f, 8);
                }
                case 3 -> { // float (Zero Gravity)
                    P.glow(level, body(e), new Vec3(P.rs() * 0.01, 0.04, P.rs() * 0.01), rgb, 0.1f, 22);
                    double ang = t * 0.3;
                    P.glow(level, base.add(Math.cos(ang) * 0.8, 0.3 + (t % 20) / 20.0 * h, Math.sin(ang) * 0.8),
                            Vec3.ZERO, 0xFFFFFF, 0.08f, 6);
                    if (t % 4 == 0) P.vanilla(level, ParticleTypes.END_ROD, body(e), new Vec3(0, 0.03, 0));
                }
                case 4 -> { // shadow (Dark Shadow)
                    P.puff(level, body(e), new Vec3(0, 0.02, 0), 0x1B1B3A, 0.3f, 0.7f, 14, 0.55f);
                    P.glow(level, body(e), Vec3.ZERO, 0x5B2C83, 0.12f, 6);
                    if (t % 3 == 0) P.vanilla(level, ParticleTypes.SQUID_INK, body(e), new Vec3(0, 0.03, 0));
                }
                case 5 -> { // wind (Fierce Wings)
                    if (t % 2 == 0) {
                        double a = t * 0.4;
                        Vec3 pos = base.add(Math.cos(a) * 0.7, P.R.nextDouble() * h, Math.sin(a) * 0.7);
                        P.feather(level, pos, new Vec3(-Math.sin(a) * 0.12, 0.03, Math.cos(a) * 0.12), 0.16f, 14, 0f);
                    }
                    P.puff(level, body(e), new Vec3(0, 0.02, 0), 0xFFFFFF, 0.12f, 0.3f, 10, 0.4f);
                }
                case 6 -> { // forge (Creation)
                    P.spark(level, body(e), P.rv(0.12).add(0, 0.05, 0), 0xE67E22, 0.1f, 10);
                    P.glow(level, body(e), Vec3.ZERO, 0xCCCCCC, 0.1f, 6);
                    if (t % 4 == 0) P.vanilla(level, ParticleTypes.CRIT, body(e), P.rv(0.15));
                }
                case 7 -> { // light (Invisibility)
                    P.glow(level, body(e), Vec3.ZERO, rgb, 0.09f, 9);
                    P.glow(level, body(e), Vec3.ZERO, 0xFFFFFF, 0.06f, 7);
                    if (t % 3 == 0) P.vanilla(level, ParticleTypes.END_ROD, body(e), Vec3.ZERO);
                }
                case 8 -> { // acid bubbles
                    P.glow(level, body(e), new Vec3(0, 0.05, 0), rgb, 0.12f + (float) P.R.nextDouble() * 0.06f, 14);
                    if (t % 5 == 0) P.vanilla(level, ParticleTypes.ITEM_SLIME, base.add(P.rs() * 0.3, 0.2, P.rs() * 0.3), Vec3.ZERO);
                }
                case 9 -> { // sound rings from the head
                    if (t % 10 == 0) {
                        Vec3 head = e.getEyePosition();
                        for (int i = 0; i < 12; i++) {
                            double a = Math.PI * 2 * i / 12;
                            P.glow(level, head, new Vec3(Math.cos(a) * 0.12, 0.0, Math.sin(a) * 0.12), rgb, 0.14f, 10);
                        }
                        P.vanilla(level, ParticleTypes.NOTE, head.add(0, 0.5, 0), new Vec3(P.R.nextDouble(), 0, 0));
                    }
                }
                case 10 -> { // fire on the left, ice on the right (Todoroki)
                    Vec3 r = rightOf(e);
                    Vec3 right = base.add(r.scale(0.4)).add(0, P.R.nextDouble() * h, 0);
                    Vec3 left = base.add(r.scale(-0.4)).add(0, P.R.nextDouble() * h, 0);
                    P.glow(level, left, new Vec3(0, 0.05, 0), 0xFF6A1F, 0.18f, 8);
                    P.glow(level, right, new Vec3(0, 0.03, 0), 0x9FE8FF, 0.16f, 10);
                    if (t % 2 == 0) {
                        P.vanilla(level, ParticleTypes.FLAME, left, new Vec3(0, 0.03, 0));
                        P.vanilla(level, ParticleTypes.SNOWFLAKE, right, new Vec3(0, -0.01, 0));
                    }
                }
                case 11 -> { // orbiting tape strips
                    for (int i = 0; i < 2; i++) {
                        double a = t * 0.35 + i * Math.PI;
                        P.tape(level, base.add(Math.cos(a) * 0.75, 0.2 + ((t * 0.07 + i * 0.5) % 1.0) * h, Math.sin(a) * 0.75),
                                rgb, 0.1f, 4);
                    }
                }
                default -> { // tendrils (Black Whip)
                    if (t % 2 == 0) {
                        Vec3 a = body(e);
                        P.bolt(level, a, a.add(P.unit().scale(0.7)), 0x4A235A, 3, 0.1);
                    }
                    P.puff(level, body(e), new Vec3(0, 0.02, 0), 0x2A1238, 0.2f, 0.45f, 12, 0.5f);
                }
            }
            return false;
        }
    }

    // ================================================================== LIGHTNING BURST

    public static final class LightningBurst extends Anim {
        private final Vec3 center;
        private final float radius;
        private final int rgb, duration;

        public LightningBurst(ClientLevel level, Vec3 center, float radius, int rgb, int duration) {
            super(level);
            this.center = center;
            this.radius = Math.max(0.8f, radius);
            this.rgb = rgb;
            this.duration = Math.max(3, duration);
        }

        @Override
        protected boolean step() {
            if (t == 0) {
                P.glowGrow(level, center, Vec3.ZERO, 0xFFFFFF, radius * 0.25f, radius * 0.8f, 4);
                P.glowGrow(level, center, Vec3.ZERO, rgb, radius * 0.4f, radius * 1.1f, 6);
                ClientAnims.shakeNear(center, 20, 8, 0.5f);
                ClientAnims.flashNear(center, 6, 4, rgb, 0.25f);
            }
            int bolts = Math.max(1, 5 - t);
            for (int i = 0; i < bolts; i++) {
                Vec3 end = center.add(P.unit().scale(radius * (0.6 + P.R.nextDouble() * 0.6)));
                P.bolt(level, center, end, rgb, 3, 0.22 * radius * 0.5 + 0.1);
            }
            for (int i = 0; i < 6; i++) {
                P.spark(level, center, P.unit().scale(0.1 + P.R.nextDouble() * 0.25 * radius), i % 2 == 0 ? rgb : 0xFFFFFF,
                        0.12f, 12);
            }
            if (t % 2 == 0) P.vanilla(level, ParticleTypes.ELECTRIC_SPARK, center.add(P.rv(radius * 0.5)), P.rv(0.2));
            return t >= duration;
        }
    }

    // ================================================================== SMASH / PUNCH

    /** Punch impact: lightning along the arm, a burst of air, a ground ring and cracks. */
    public static final class Smash extends Anim {
        private final int src, rgb;
        private final Vec3 impact, dir;
        private final float intensity;

        public Smash(ClientLevel level, int src, Vec3 impact, Vec3 dir, int rgb, float intensity) {
            super(level);
            this.src = src;
            this.impact = impact;
            this.dir = dir.lengthSqr() < 1e-4 ? new Vec3(0, 0, 1) : dir.normalize();
            this.rgb = rgb;
            this.intensity = Math.max(0.3f, intensity);
        }

        @Override
        protected boolean step() {
            Entity e = ent(src);
            double gy = e != null ? e.getY() : impact.y;
            Vec3 g = new Vec3(impact.x, gy + 0.05, impact.z);
            if (t == 0) {
                Vec3 hand = e != null ? hand(e) : impact.subtract(dir.scale(2.5));
                for (int i = 0; i < 3; i++) P.bolt(level, hand, impact, rgb, 3, 0.35);
                P.glowGrow(level, impact, Vec3.ZERO, 0xFFFFFF, 0.5f * intensity, 1.7f * intensity, 4);
                P.glowGrow(level, impact, Vec3.ZERO, rgb, 0.7f * intensity, 2.0f * intensity, 6);
                P.vanilla(level, ParticleTypes.EXPLOSION, impact, new Vec3(0.7, 0, 0));
                int n = (int) (22 * intensity);
                for (int i = 0; i < n; i++) {
                    Vec3 v = dir.scale(0.8 + P.R.nextDouble() * 0.9).add(P.rv(0.2));
                    P.spark(level, impact, v, i % 3 == 0 ? 0xFFFFFF : rgb, 0.14f, 9);
                }
                for (int i = 0; i < 10; i++) {
                    P.vanilla(level, ParticleTypes.ELECTRIC_SPARK, impact.add(P.rv(0.5)), P.rv(0.3));
                }
                ClientAnims.shakeNear(impact, 18, 6, 0.65f * intensity);
            }
            if (t >= 1 && t <= 6) {
                double r = 0.8 + t * 0.55 * intensity;
                int cnt = 16 + (int) (r * 6);
                for (int i = 0; i < cnt; i++) {
                    double a = Math.PI * 2 * i / cnt;
                    Vec3 pos = g.add(Math.cos(a) * r, 0, Math.sin(a) * r);
                    Vec3 out = new Vec3(Math.cos(a) * 0.04, 0.02, Math.sin(a) * 0.04);
                    if (i % 2 == 0) P.puff(level, pos, out, 0xDDDDDD, 0.18f, 0.45f, 8, 0.5f);
                    else P.glow(level, pos, out, rgb, 0.15f, 4);
                }
            }
            if (t >= 1 && t <= 3) {
                BlockParticleOption chips = new BlockParticleOption(ParticleTypes.BLOCK, ground(level, g));
                for (int line = 0; line < 6; line++) {
                    double a = Math.PI * 2 * line / 6 + 0.3;
                    Vec3 pos = g.add(Math.cos(a) * t * 0.9 * intensity, 0.1, Math.sin(a) * t * 0.9 * intensity);
                    P.vanilla(level, chips, pos, new Vec3(0, 0.22, 0));
                    P.vanilla(level, chips, pos.add(P.rv(0.15)), new Vec3(P.rs() * 0.05, 0.25, P.rs() * 0.05));
                }
            }
            return t >= 7;
        }
    }

    // ================================================================== LEAP

    public static final class Leap extends Anim {
        private final int src, rgb;

        public Leap(ClientLevel level, int src, int rgb) {
            super(level);
            this.src = src;
            this.rgb = rgb;
        }

        @Override
        protected boolean step() {
            Entity e = ent(src);
            if (e == null || e.isRemoved()) return true;
            Vec3 base = e.position();
            if (t == 0) {
                for (int i = 0; i < 18; i++) {
                    double a = Math.PI * 2 * i / 18;
                    P.puff(level, base.add(Math.cos(a) * 0.6, 0.1, Math.sin(a) * 0.6),
                            new Vec3(Math.cos(a) * 0.12, 0.03, Math.sin(a) * 0.12), 0xDDDDDD, 0.2f, 0.55f, 12, 0.6f);
                }
                for (int i = 0; i < 3; i++) {
                    P.bolt(level, base.add(0, 0.1, 0), base.add(P.rs() * 0.7, 1.4 + P.R.nextDouble(), P.rs() * 0.7), rgb, 3, 0.2);
                }
                P.glowGrow(level, base.add(0, 0.2, 0), Vec3.ZERO, rgb, 0.3f, 1.2f, 5);
                BlockParticleOption chips = new BlockParticleOption(ParticleTypes.BLOCK, ground(level, base));
                for (int i = 0; i < 16; i++) P.vanilla(level, chips, base.add(P.rs() * 0.6, 0.1, P.rs() * 0.6), new Vec3(P.rs() * 0.1, 0.25, P.rs() * 0.1));
                ClientAnims.shakeNear(base, 12, 5, 0.3f);
            }
            P.glow(level, base.add(P.rs() * 0.2, 0.2, P.rs() * 0.2), new Vec3(0, -0.04, 0), rgb, 0.28f, 9);
            P.spark(level, base.add(0, 0.3, 0), P.rv(0.12), rgb, 0.12f, 10);
            if (t % 2 == 0) {
                Vec3 a = base.add(P.rs() * 0.3, 0.1, P.rs() * 0.3);
                P.bolt(level, a, a.add(P.rs() * 0.4, 1.2, P.rs() * 0.4), rgb, 2, 0.15);
            }
            return t >= 12;
        }
    }

    // ================================================================== SHARDS

    public static final class Shards extends Anim {
        private final Vec3 center;
        private final float scale;
        private final int rgb;

        public Shards(ClientLevel level, Vec3 center, float scale, int rgb) {
            super(level);
            this.center = center;
            this.scale = Math.max(0.5f, scale);
            this.rgb = rgb;
        }

        @Override
        protected boolean step() {
            if (t > 0) return true;
            P.glowGrow(level, center, Vec3.ZERO, 0xFFFFFF, 0.3f * scale, 1.0f * scale, 4);
            int n = (int) (36 * scale);
            for (int i = 0; i < n; i++) {
                Vec3 v = P.unit().scale((0.2 + P.R.nextDouble() * 0.4) * scale);
                P.spark(level, center, v, i % 3 == 0 ? 0xCCCCCC : rgb, 0.14f, 18);
            }
            for (int i = 0; i < 14; i++) P.vanilla(level, ParticleTypes.CRIT, center, P.unit().scale(0.5));
            for (int i = 0; i < 20; i++) {
                double a = Math.PI * 2 * i / 20;
                P.glow(level, center, new Vec3(Math.cos(a) * 0.18 * scale, 0, Math.sin(a) * 0.18 * scale), rgb, 0.18f, 8);
            }
            ClientAnims.shakeNear(center, 12, 5, 0.25f * scale);
            return false;
        }
    }

    // ================================================================== RING BURST

    public static final class RingBurst extends Anim {
        private final Vec3 center;
        private final float radius;
        private final int rgb, duration;

        public RingBurst(ClientLevel level, Vec3 center, float radius, int rgb, int duration) {
            super(level);
            this.center = center;
            this.radius = Math.max(0.5f, radius);
            this.rgb = rgb;
            this.duration = Math.max(2, duration);
        }

        @Override
        protected boolean step() {
            double gy = groundY(center) + 0.1;
            double r = radius * (t + 1) / duration;
            int cnt = 10 + (int) (r * 8);
            for (int i = 0; i < cnt; i++) {
                double a = Math.PI * 2 * i / cnt;
                Vec3 pos = new Vec3(center.x + Math.cos(a) * r, gy, center.z + Math.sin(a) * r);
                Vec3 out = new Vec3(Math.cos(a) * 0.03, 0.02, Math.sin(a) * 0.03);
                P.glow(level, pos, out, rgb, 0.17f, 4);
                if (i % 2 == 0) P.puff(level, pos, out, 0xDDDDDD, 0.15f, 0.35f, 8, 0.4f);
            }
            return t + 1 >= duration;
        }
    }

    // ================================================================== SPEED TRAIL

    public static final class SpeedTrail extends Anim {
        private final int src, rgb, ticks, style;

        public SpeedTrail(ClientLevel level, int src, int rgb, int ticks, int style) {
            super(level);
            this.src = src;
            this.rgb = rgb;
            this.ticks = ticks;
            this.style = style;
        }

        @Override
        protected boolean step() {
            Entity e = ent(src);
            if (e == null || e.isRemoved() || t >= ticks) return true;
            Vec3 mv = e.getDeltaMovement();
            double speed = mv.length();
            Vec3 feet = e.position();
            for (int side = -1; side <= 1; side += 2) {
                Vec3 calf = feet.add(rightOf(e).scale(0.18 * side)).add(0, 0.25, 0);
                P.glow(level, calf, mv.scale(-0.12), rgb, 0.22f, 8);
                P.puff(level, calf, mv.scale(-0.08).add(P.rv(0.02)), 0xFFFFFF, 0.12f, 0.35f, 9, 0.5f);
            }
            P.glow(level, feet.add(0, e.getBbHeight() * 0.5, 0), mv.scale(-0.1), rgb, 0.2f, 8);
            if (speed > 0.3) {
                Vec3 from = feet.add(0, e.getBbHeight() * 0.5, 0);
                P.line(level, from, from.subtract(mv.scale(3.5)), rgb, 2);
            }
            if (style == 1 && t % 2 == 0) {
                Vec3 a = body(e);
                P.bolt(level, a, a.add(P.unit().scale(0.8)), rgb, 2, 0.12);
            }
            return false;
        }
    }

    // ================================================================== GATHER

    /** Particles streaming into a point, then a flash. */
    public static final class Gather extends Anim {
        private final Vec3 center;
        private final float radius;
        private final int rgb, ticks;

        public Gather(ClientLevel level, Vec3 center, float radius, int rgb, int ticks) {
            super(level);
            this.center = center;
            this.radius = Math.max(0.5f, radius);
            this.rgb = rgb;
            this.ticks = Math.max(2, ticks);
        }

        @Override
        protected boolean step() {
            if (t < ticks) {
                int remaining = ticks - t;
                for (int i = 0; i < 9; i++) {
                    Vec3 start = center.add(P.unit().scale(radius * (0.7 + P.R.nextDouble() * 0.5)));
                    Vec3 v = center.subtract(start).scale(1.0 / remaining);
                    if (i % 3 == 0) P.spark(level, start, v, rgb, 0.12f, remaining);
                    else P.glow(level, start, v, i % 2 == 0 ? rgb : 0xFFFFFF, 0.13f, remaining);
                }
                return false;
            }
            P.glowGrow(level, center, Vec3.ZERO, 0xFFFFFF, 0.3f, radius * 0.6f, 5);
            P.glowGrow(level, center, Vec3.ZERO, rgb, 0.4f, radius * 0.9f, 7);
            for (int i = 0; i < 20; i++) P.spark(level, center, P.unit().scale(0.15 + P.R.nextDouble() * 0.25), rgb, 0.12f, 14);
            return true;
        }
    }

    // ================================================================== PROJECTILE

    /** style 0: glowing bolt, 1: acid glob on an arc, 2: fast light streak. */
    public static final class Projectile extends Anim {
        private final Vec3 from, to;
        private final int rgb, style, travel;
        private final double dist;

        public Projectile(ClientLevel level, Vec3 from, Vec3 to, int rgb, int style, int travelTicks) {
            super(level);
            this.from = from;
            this.to = to;
            this.rgb = rgb;
            this.style = style;
            this.dist = from.distanceTo(to);
            this.travel = travelTicks > 0 ? travelTicks : Math.max(2, Math.min(12, (int) (dist / 2.2)));
        }

        private Vec3 at(double u) {
            Vec3 p = from.lerp(to, u);
            if (style == 1) p = p.add(0, Math.sin(u * Math.PI) * dist * 0.12, 0);
            return p;
        }

        @Override
        protected boolean step() {
            double u0 = (double) t / travel;
            double u1 = Math.min(1.0, (t + 1.0) / travel);
            for (int s = 1; s <= 3; s++) {
                Vec3 p = at(u0 + (u1 - u0) * s / 3.0);
                switch (style) {
                    case 0 -> {
                        P.glow(level, p, Vec3.ZERO, rgb, 0.3f, 4);
                        P.glow(level, p, Vec3.ZERO, 0xFFFFFF, 0.14f, 3);
                        if (s == 3) P.spark(level, p, P.rv(0.08), rgb, 0.1f, 10);
                    }
                    case 1 -> {
                        P.glow(level, p, Vec3.ZERO, rgb, 0.24f, 4);
                        P.glow(level, p, Vec3.ZERO, 0xE8FFB0, 0.1f, 3);
                        if (s == 3) P.spark(level, p, P.rv(0.04).add(0, -0.02, 0), rgb, 0.1f, 14);
                    }
                    default -> {
                        P.glow(level, p, Vec3.ZERO, 0xFFFFFF, 0.1f, 6);
                        P.glow(level, p, Vec3.ZERO, rgb, 0.2f, 8);
                    }
                }
            }
            if (u1 >= 1.0) {
                impact();
                return true;
            }
            return false;
        }

        private void impact() {
            switch (style) {
                case 0 -> {
                    P.glowGrow(level, to, Vec3.ZERO, 0xFFFFFF, 0.3f, 1.0f, 4);
                    P.glowGrow(level, to, Vec3.ZERO, rgb, 0.4f, 1.4f, 6);
                    for (int i = 0; i < 22; i++) P.spark(level, to, P.unit().scale(0.15 + P.R.nextDouble() * 0.3), rgb, 0.12f, 14);
                    BlockParticleOption iron = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.IRON_BLOCK.defaultBlockState());
                    for (int i = 0; i < 12; i++) P.vanilla(level, iron, to, P.unit().scale(0.25));
                    ClientAnims.shakeNear(to, 12, 4, 0.3f);
                }
                case 1 -> {
                    for (int i = 0; i < 16; i++) P.vanilla(level, ParticleTypes.ITEM_SLIME, to, P.unit().scale(0.2));
                    for (int i = 0; i < 18; i++) P.spark(level, to, P.unit().scale(0.1 + P.R.nextDouble() * 0.2), rgb, 0.12f, 16);
                    P.glowGrow(level, to, Vec3.ZERO, rgb, 0.3f, 1.1f, 6);
                    for (int i = 0; i < 12; i++) {
                        double a = Math.PI * 2 * i / 12;
                        P.glow(level, to, new Vec3(Math.cos(a) * 0.14, 0.02, Math.sin(a) * 0.14), rgb, 0.16f, 7);
                    }
                }
                default -> {
                    P.vanilla(level, ParticleTypes.SWEEP_ATTACK, to, Vec3.ZERO);
                    P.glowGrow(level, to, Vec3.ZERO, 0xFFFFFF, 0.2f, 0.8f, 4);
                    for (int i = 0; i < 14; i++) P.spark(level, to, P.unit().scale(0.2 + P.R.nextDouble() * 0.25), rgb, 0.1f, 10);
                }
            }
        }
    }

    // ================================================================== SHIMMER / DOME

    public static final class Shimmer extends Anim {
        private final int src, rgb, ticks;

        public Shimmer(ClientLevel level, int src, int rgb, int ticks) {
            super(level);
            this.src = src;
            this.rgb = rgb;
            this.ticks = ticks;
        }

        @Override
        protected boolean step() {
            Entity e = ent(src);
            if (e == null || e.isRemoved() || t >= ticks) return true;
            if (t == 0) {
                for (int i = 0; i < 30; i++) {
                    P.glow(level, body(e), P.rv(0.08), rgb, 0.14f, 14);
                    P.vanilla(level, ParticleTypes.END_ROD, body(e), P.rv(0.06));
                }
            }
            for (int i = 0; i < 3; i++) P.glow(level, body(e), new Vec3(0, 0.01, 0), rgb, 0.08f, 10);
            if (t % 3 == 0) P.vanilla(level, ParticleTypes.END_ROD, body(e), Vec3.ZERO);
            if (t % 8 == 0) {
                Vec3 c = e.position().add(0, e.getBbHeight() * 0.5, 0);
                for (int i = 0; i < 14; i++) {
                    double a = Math.PI * 2 * i / 14;
                    P.glow(level, c, new Vec3(Math.cos(a) * 0.07, 0, Math.sin(a) * 0.07), rgb, 0.09f, 9);
                }
            }
            return false;
        }
    }

    public static final class Dome extends Anim {
        private final Vec3 center;
        private final float radius;
        private final int rgb, ticks;

        public Dome(ClientLevel level, Vec3 center, float radius, int rgb, int ticks) {
            super(level);
            this.center = center;
            this.radius = Math.max(1f, radius);
            this.rgb = rgb;
            this.ticks = ticks;
        }

        private Vec3 onDome(double r) {
            Vec3 d = P.unit();
            return center.add(d.x * r, Math.abs(d.y) * r, d.z * r);
        }

        @Override
        protected boolean step() {
            if (t < 8) {
                double r = radius * (t + 1) / 8.0;
                for (int i = 0; i < 40; i++) P.glow(level, onDome(r), Vec3.ZERO, rgb, 0.2f, 6);
                double gy = groundY(center) + 0.1;
                int cnt = 12 + (int) (r * 5);
                for (int i = 0; i < cnt; i++) {
                    double a = Math.PI * 2 * i / cnt;
                    P.glow(level, new Vec3(center.x + Math.cos(a) * r, gy, center.z + Math.sin(a) * r), Vec3.ZERO, 0xFFFFFF, 0.1f, 4);
                }
            } else {
                for (int i = 0; i < 6; i++) P.glow(level, onDome(radius), Vec3.ZERO, rgb, 0.1f, 8);
                if (t % 3 == 0) P.vanilla(level, ParticleTypes.END_ROD, onDome(radius * 0.9), Vec3.ZERO);
            }
            return t >= ticks;
        }
    }
}
