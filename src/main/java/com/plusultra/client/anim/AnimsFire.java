package com.plusultra.client.anim;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Explosions, fire, ice, meteors and gravity. */
public final class AnimsFire {
    private AnimsFire() {}

    private static BlockState ground(ClientLevel level, Vec3 at) {
        BlockState s = level.getBlockState(BlockPos.containing(at.x, at.y - 0.5, at.z));
        return s.isAir() ? Blocks.DIRT.defaultBlockState() : s;
    }

    // ================================================================== MHA EXPLOSION

    /**
     * A staged, anime-style explosion: white flash, a fireball that swells from yellow to orange to red,
     * crackling sparks, a ground shock ring, debris, then billowing black smoke.
     */
    public static final class MhaExplosion extends Anim {
        private final Vec3 a;
        private final float radius;
        private final int rgb;

        public MhaExplosion(ClientLevel level, Vec3 center, float radius, int rgb) {
            super(level);
            this.a = center;
            this.radius = Math.max(1.0f, radius);
            this.rgb = rgb;
        }

        @Override
        protected boolean step() {
            double R = radius;
            if (t == 0) {
                P.glowGrow(level, a, Vec3.ZERO, 0xFFFFFF, (float) (R * 0.8), (float) (R * 2.2), 3);
                P.vanilla(level, ParticleTypes.FLASH, a, Vec3.ZERO);
                for (int i = 0; i < 48; i++) {
                    double sp = (0.25 + P.R.nextDouble() * 0.55) * R * 0.45;
                    int col = i % 3 == 0 ? 0xFFE08A : i % 3 == 1 ? rgb : 0xFF4500;
                    P.spark(level, a, P.unit().scale(sp), col, 0.14f, 16 + P.R.nextInt(14));
                }
                for (int i = 0; i < 10; i++) P.vanilla(level, ParticleTypes.LAVA, a, P.unit().scale(0.2));
                BlockParticleOption chips = new BlockParticleOption(ParticleTypes.BLOCK, ground(level, a));
                double gy = groundY(a);
                for (int i = 0; i < 26; i++) {
                    Vec3 p = new Vec3(a.x + P.rs() * R * 0.6, gy + 0.2, a.z + P.rs() * R * 0.6);
                    P.vanilla(level, chips, p, new Vec3(P.rs() * 0.2, 0.3 + R * 0.12 * P.R.nextDouble(), P.rs() * 0.2));
                }
                ClientAnims.shakeNear(a, R * 6 + 10, 10 + (int) (R * 2), (float) Math.min(1.6, 0.5 + R * 0.18));
                ClientAnims.flashNear(a, R * 4 + 6, 5, 0xFFF2D0, (float) Math.min(0.75, 0.35 + R * 0.08));
            }

            if (t < 9) {
                double u = (t + 1) / 9.0;
                double r = R * (0.25 + 0.75 * (1 - Math.pow(1 - u, 2)));
                int n = 20 + (int) (R * 10);
                int col = u < 0.35 ? 0xFFF2A8 : u < 0.65 ? rgb : 0xD2410D;
                float size = (float) Math.min(1.4, 0.45 * R * (1 - 0.4 * u));
                for (int i = 0; i < n; i++) {
                    Vec3 d = P.fib(i, n, t * 0.7);
                    Vec3 p = a.add(d.scale(r * (0.85 + 0.15 * P.R.nextDouble())));
                    P.glow(level, p, d.scale(0.05), col, size, 5);
                }
                for (int i = 0; i < 8; i++) {
                    Vec3 p = a.add(P.unit().scale(r * Math.cbrt(P.R.nextDouble())));
                    P.glow(level, p, Vec3.ZERO, P.mix(0xFFF2A8, col, 0.5), size * 1.1f, 4);
                }
            }

            if (t >= 2 && t < 16) {
                for (int i = 0; i < 4; i++) {
                    Vec3 p = a.add(P.unit().scale(R * 0.7 * P.R.nextDouble())).add(0, t * 0.04 * R, 0);
                    P.puff(level, p, new Vec3(P.rs() * 0.02, 0.06, P.rs() * 0.02), i % 2 == 0 ? 0x2A2A2A : 0x4A3A32,
                            (float) (0.6 * R), (float) (1.7 * R), 28 + P.R.nextInt(14), 0.8f);
                }
            }
            if (t == 3) {
                for (int i = 0; i < 16; i++) P.vanilla(level, ParticleTypes.LARGE_SMOKE, a.add(P.rv(R * 0.4)), new Vec3(0, 0.08, 0));
            }
            if (t < 10 && t % 2 == 0) {
                P.vanilla(level, ParticleTypes.EXPLOSION, a.add(P.unit().scale(R * 0.8)), new Vec3(0.7, 0, 0));
            }
            if (t >= 1 && t <= 9) {
                double rr = R * 0.4 + t * R * 0.38;
                double gy = groundY(a) + 0.1;
                int cnt = 18 + (int) (rr * 6);
                for (int i = 0; i < cnt; i++) {
                    double ang = Math.PI * 2 * i / cnt;
                    Vec3 pos = new Vec3(a.x + Math.cos(ang) * rr, gy, a.z + Math.sin(ang) * rr);
                    Vec3 out = new Vec3(Math.cos(ang) * 0.1, 0.01, Math.sin(ang) * 0.1);
                    P.puff(level, pos, out, 0xE8D8C0, 0.25f, 0.55f, 8, 0.6f);
                    if (i % 3 == 0) P.glow(level, pos, out, 0xFFD27A, 0.2f, 4);
                }
            }
            return t >= 18;
        }
    }

    // ================================================================== PALM CRACKLE

    public static final class PalmCrackle extends Anim {
        private final int src, ticks;

        public PalmCrackle(ClientLevel level, int src, int ticks) {
            super(level);
            this.src = src;
            this.ticks = ticks;
        }

        @Override
        protected boolean step() {
            Entity e = ent(src);
            if (e == null || e.isRemoved() || t >= ticks) return true;
            for (int side = -1; side <= 1; side += 2) {
                Vec3 f = P.flat(e.getLookAngle());
                Vec3 r = new Vec3(-f.z, 0, f.x);
                Vec3 h = e.getEyePosition().add(f.scale(0.4)).add(r.scale(0.35 * side)).add(0, -0.5, 0);
                for (int i = 0; i < 2; i++) P.spark(level, h, P.rv(0.18), i == 0 ? 0xFFD27A : 0xFF8C1A, 0.1f, 8);
                P.glow(level, h, Vec3.ZERO, 0xFFE08A, 0.14f, 3);
                if (t % 2 == 0) P.vanilla(level, ParticleTypes.FLAME, h, P.rv(0.03));
            }
            return false;
        }
    }

    // ================================================================== BLAST JET

    public static final class BlastJet extends Anim {
        private final int src, ticks;
        private final Vec3 dir;

        public BlastJet(ClientLevel level, int src, Vec3 dir, int ticks) {
            super(level);
            this.src = src;
            this.dir = dir.lengthSqr() < 1e-4 ? new Vec3(0, 0, 1) : dir.normalize();
            this.ticks = Math.max(2, ticks);
        }

        @Override
        protected boolean step() {
            Entity e = ent(src);
            if (e == null || e.isRemoved() || t >= ticks) return true;
            Vec3 pos = e.position().add(0, 0.9, 0).subtract(dir.scale(0.5));
            P.puff(level, pos, dir.scale(-0.22).add(P.rv(0.05)), t % 2 == 0 ? 0xFF8C1A : 0x3A3A3A, 0.25f, 0.9f, 10, 0.8f);
            P.glow(level, pos, Vec3.ZERO, 0xFFD27A, 0.4f, 3);
            for (int i = 0; i < 4; i++) P.spark(level, pos, dir.scale(-0.5).add(P.rv(0.15)), 0xFFB347, 0.1f, 10);
            if (t % 2 == 0) {
                P.vanilla(level, ParticleTypes.EXPLOSION, pos.add(P.rv(0.3)), new Vec3(0.85, 0, 0));
                P.vanilla(level, ParticleTypes.FLAME, pos, dir.scale(-0.2).add(P.rv(0.05)));
            }
            P.glow(level, hand(e), Vec3.ZERO, 0xFFE08A, 0.25f, 3);
            return false;
        }
    }

    // ================================================================== HOWITZER WINDUP

    /** Spinning fire spiral around the player, then a huge explosion at the target. */
    public static final class HowitzerWindup extends Anim {
        private final int src, rgb, windup;
        private final Vec3 target;
        private final float radius;

        public HowitzerWindup(ClientLevel level, int src, Vec3 target, float radius, int rgb, int windup) {
            super(level);
            this.src = src;
            this.target = target;
            this.radius = radius;
            this.rgb = rgb;
            this.windup = Math.max(3, windup);
        }

        @Override
        protected boolean step() {
            Entity e = ent(src);
            if (t >= windup) {
                if (e != null) P.line(level, hand(e), target, 0xFFE08A, 3);
                ClientAnims.add(new MhaExplosion(level, target, radius, rgb));
                return true;
            }
            if (e == null || e.isRemoved()) {
                ClientAnims.add(new MhaExplosion(level, target, radius, rgb));
                return true;
            }
            Vec3 c = e.position();
            for (int k = 0; k < 3; k++) {
                double ang = t * 0.9 + k * 2.094;
                double rad = 0.9 + 0.12 * t;
                Vec3 pos = new Vec3(c.x + Math.cos(ang) * rad, c.y + 0.2 + ((t * 0.3 + k * 0.7) % 2.0), c.z + Math.sin(ang) * rad);
                Vec3 tang = new Vec3(-Math.sin(ang) * 0.15, 0.05, Math.cos(ang) * 0.15);
                P.glow(level, pos, tang, rgb, 0.24f, 6);
                P.vanilla(level, ParticleTypes.FLAME, pos, tang.scale(0.3));
                P.spark(level, pos, tang, 0xFFD27A, 0.1f, 8);
            }
            float grow = 0.1f + 0.07f * t;
            P.glow(level, hand(e), Vec3.ZERO, 0xFFE08A, grow, 2);
            if (t % 2 == 0) P.vanilla(level, ParticleTypes.SMOKE, c.add(P.rs() * 0.6, 0.5, P.rs() * 0.6), new Vec3(0, 0.05, 0));
            return false;
        }
    }

    // ================================================================== FLAME JET / WIND GUST

    /** m = 0: stream of fire. m = 1: gust of wind. Follows where the caster looks for a few ticks. */
    public static final class FlameJet extends Anim {
        private final int src, rgb, style;
        private final float range;
        private final Vec3 fallbackOrigin, fallbackDir;

        public FlameJet(ClientLevel level, int src, Vec3 origin, Vec3 dir, float range, int rgb, int style) {
            super(level);
            this.src = src;
            this.fallbackOrigin = origin;
            this.fallbackDir = dir.lengthSqr() < 1e-4 ? new Vec3(0, 0, 1) : dir.normalize();
            this.range = Math.max(2f, range);
            this.rgb = rgb;
            this.style = style;
        }

        @Override
        protected boolean step() {
            if (t >= 7) return true;
            Entity e = ent(src);
            Vec3 origin = e != null ? hand(e) : fallbackOrigin;
            Vec3 dir = e != null ? e.getLookAngle() : fallbackDir;
            double speed = range * 0.17;
            if (t == 0) P.glowGrow(level, origin, Vec3.ZERO, style == 0 ? 0xFFE08A : 0xFFFFFF, 0.15f, 0.8f, 4);
            for (int i = 0; i < 7; i++) {
                Vec3 v = dir.scale(speed * (0.85 + P.R.nextDouble() * 0.3)).add(P.rv(0.08));
                if (style == 0) {
                    P.glow(level, origin, v, i % 2 == 0 ? rgb : 0xFFC04A, 0.28f, 8);
                } else {
                    P.glow(level, origin, v, i % 2 == 0 ? 0xFFFFFF : rgb, 0.18f, 8);
                }
            }
            for (int i = 0; i < 4; i++) {
                Vec3 v = dir.scale(speed * 0.7).add(P.rv(0.06));
                if (style == 0) P.puff(level, origin, v, i % 2 == 0 ? rgb : 0x553322, 0.3f, 0.9f, 12, 0.7f);
                else P.puff(level, origin, v, 0xFFFFFF, 0.25f, 0.8f, 12, 0.45f);
            }
            if (style == 0) {
                for (int i = 0; i < 2; i++) P.vanilla(level, ParticleTypes.FLAME, origin, dir.scale(speed * 0.5).add(P.rv(0.06)));
                P.vanilla(level, ParticleTypes.SMOKE, origin, dir.scale(speed * 0.4).add(P.rv(0.05)));
            } else {
                for (int i = 0; i < 2; i++) P.vanilla(level, ParticleTypes.CLOUD, origin, dir.scale(speed * 0.6).add(P.rv(0.08)));
            }
            return false;
        }
    }

    // ================================================================== ICE WAVE

    /** Spikes of ice erupt in a widening V along the ground. */
    public static final class IceWave extends Anim {
        private final Vec3 start, dir;
        private final float range;
        private final int rgb;

        public IceWave(ClientLevel level, Vec3 start, Vec3 dir, float range, int rgb) {
            super(level);
            this.start = start;
            this.dir = P.flat(dir).lengthSqr() < 1e-4 ? new Vec3(0, 0, 1) : P.flat(dir);
            this.range = Math.max(2f, range);
            this.rgb = rgb;
        }

        @Override
        protected boolean step() {
            if (t >= 7) return true;
            if (t == 0) {
                for (int i = 0; i < 16; i++) {
                    double a = Math.PI * 2 * i / 16;
                    P.glow(level, start.add(0, 0.1, 0), new Vec3(Math.cos(a) * 0.1, 0.01, Math.sin(a) * 0.1), rgb, 0.16f, 7);
                }
            }
            double d = 1.2 + (t + 1) * (range - 1.2) / 7.0;
            Vec3 c = start.add(dir.scale(d));
            Vec3 lat = P.perp(dir);
            BlockParticleOption ice = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ICE.defaultBlockState());
            for (int lane = -1; lane <= 1; lane++) {
                double off = lane * (0.8 + t * 0.15);
                Vec3 s = c.add(lat.scale(off));
                double gy = groundY(new Vec3(s.x, start.y + 1.5, s.z));
                double height = (lane == 0 ? 1.7 : 1.0) + P.R.nextDouble() * 0.5;
                for (double h = 0; h <= height; h += 0.2) {
                    float taper = (float) (1.0 - h / (height + 0.3));
                    P.glow(level, new Vec3(s.x, gy + h, s.z), Vec3.ZERO, (int) (h * 5) % 2 == 0 ? 0xE8FBFF : rgb,
                            0.06f + 0.2f * taper, 20);
                }
                for (int i = 0; i < 4; i++) P.vanilla(level, ice, new Vec3(s.x, gy + 0.2, s.z), new Vec3(P.rs() * 0.1, 0.2, P.rs() * 0.1));
                for (int i = 0; i < 3; i++) P.vanilla(level, ParticleTypes.SNOWFLAKE, new Vec3(s.x, gy + 0.4, s.z), P.rv(0.08));
                P.puff(level, new Vec3(s.x, gy + 0.2, s.z), new Vec3(0, 0.03, 0), 0xDFF8FF, 0.3f, 0.8f, 16, 0.45f);
            }
            return false;
        }
    }

    // ================================================================== DUAL NOVA

    /** Half fire, half ice, expanding outward. */
    public static final class DualNova extends Anim {
        private final Vec3 center;
        private final float radius;

        public DualNova(ClientLevel level, Vec3 center, float radius) {
            super(level);
            this.center = center;
            this.radius = Math.max(2f, radius);
        }

        @Override
        protected boolean step() {
            double gy = groundY(center) + 0.2;
            if (t == 0) {
                P.glowGrow(level, center.add(0, 1, 0), Vec3.ZERO, 0xFFFFFF, 0.6f, 2.4f, 4);
                ClientAnims.shakeNear(center, 24, 12, 0.9f);
                ClientAnims.flashNear(center, 8, 4, 0xDDF4FF, 0.35f);
            }
            if (t < 10) {
                double r = radius * (t + 1) / 10.0;
                int cnt = 24 + (int) (r * 8);
                for (int i = 0; i < cnt; i++) {
                    double ang = Math.PI * 2 * i / cnt;
                    Vec3 pos = new Vec3(center.x + Math.cos(ang) * r, gy, center.z + Math.sin(ang) * r);
                    if (ang < Math.PI) {
                        P.glow(level, pos, new Vec3(0, 0.08, 0), 0xFF6A1F, 0.24f, 6);
                        if (i % 3 == 0) P.vanilla(level, ParticleTypes.FLAME, pos, new Vec3(0, 0.1, 0));
                    } else {
                        P.glow(level, pos, new Vec3(0, 0.05, 0), 0x9FE8FF, 0.24f, 6);
                        if (i % 3 == 0) P.vanilla(level, ParticleTypes.SNOWFLAKE, pos, new Vec3(0, 0.05, 0));
                    }
                }
                for (int k = 0; k < 2; k++) {
                    double ang = t * 0.7 + k * Math.PI;
                    Vec3 pos = center.add(Math.cos(ang) * 1.3, 0.3 + t * 0.4, Math.sin(ang) * 1.3);
                    P.glow(level, pos, Vec3.ZERO, k == 0 ? 0xFF5A1F : 0x9FE8FF, 0.3f, 6);
                }
            }
            if (t == 5) {
                BlockParticleOption ice = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ICE.defaultBlockState());
                for (int i = 0; i < 8; i++) {
                    double ang = P.R.nextDouble() * Math.PI * 2;
                    double r = radius * (0.5 + 0.5 * P.R.nextDouble());
                    Vec3 base = new Vec3(center.x + Math.cos(ang) * r, gy, center.z + Math.sin(ang) * r);
                    boolean fire = i % 2 == 0;
                    for (double h = 0; h < 3.0; h += 0.25) {
                        Vec3 p = base.add(0, h, 0);
                        P.glow(level, p, new Vec3(0, fire ? 0.05 : 0.0, 0), fire ? 0xFF6A1F : 0xCFF6FF,
                                0.32f - (float) h * 0.07f, 14);
                        if (fire) P.vanilla(level, ParticleTypes.FLAME, p, new Vec3(0, 0.1, 0));
                        else P.vanilla(level, ice, p, new Vec3(0, 0.1, 0));
                    }
                }
            }
            return t >= 14;
        }
    }

    // ================================================================== METEORS

    private static final class Meteor {
        Vec3 pos;
        final Vec3 vel;
        final Vec3 target;
        int life;

        Meteor(Vec3 pos, Vec3 target, int travel) {
            this.pos = pos;
            this.target = target;
            this.vel = target.subtract(pos).scale(1.0 / travel);
            this.life = travel;
        }
    }

    public static final class Meteors extends Anim {
        private final Vec3 center;
        private final float radius;
        private final int rgb;
        private final List<Meteor> meteors = new ArrayList<>();

        public Meteors(ClientLevel level, Vec3 center, float radius, int rgb) {
            super(level);
            this.center = center;
            this.radius = Math.max(2f, radius);
            this.rgb = rgb;
        }

        @Override
        protected boolean step() {
            if (t < 18) {
                for (int k = 0; k < 1 + (t % 2); k++) {
                    double ang = P.R.nextDouble() * Math.PI * 2;
                    double r = radius * Math.sqrt(P.R.nextDouble());
                    Vec3 flat = new Vec3(center.x + Math.cos(ang) * r, center.y, center.z + Math.sin(ang) * r);
                    Vec3 target = new Vec3(flat.x, groundY(flat), flat.z);
                    int travel = 9 + P.R.nextInt(4);
                    Vec3 start = target.add(P.rs() * 5, 22 + P.R.nextDouble() * 6, P.rs() * 5);
                    meteors.add(new Meteor(start, target, travel));
                }
            }
            Iterator<Meteor> it = meteors.iterator();
            while (it.hasNext()) {
                Meteor m = it.next();
                for (int s = 1; s <= 3; s++) {
                    Vec3 p = m.pos.add(m.vel.scale(s / 3.0));
                    P.glow(level, p, Vec3.ZERO, rgb, 0.3f, 6);
                    P.glow(level, p, Vec3.ZERO, 0xFFFFFF, 0.14f, 3);
                    if (s == 3) {
                        P.puff(level, p, new Vec3(0, 0.02, 0), 0x666666, 0.25f, 0.6f, 10, 0.5f);
                        P.vanilla(level, ParticleTypes.FLAME, p, P.rv(0.03));
                    }
                }
                P.glow(level, m.pos.add(m.vel), Vec3.ZERO, 0xFFE8D0, 0.5f, 2);
                m.pos = m.pos.add(m.vel);
                if (--m.life <= 0) {
                    impact(m.target);
                    it.remove();
                }
            }
            return t >= 18 && meteors.isEmpty();
        }

        private void impact(Vec3 at) {
            P.glowGrow(level, at.add(0, 0.3, 0), Vec3.ZERO, 0xFFFFFF, 0.3f, 1.2f, 4);
            for (int i = 0; i < 14; i++) P.spark(level, at.add(0, 0.3, 0), P.unit().scale(0.15 + P.R.nextDouble() * 0.25), rgb, 0.12f, 14);
            for (int i = 0; i < 12; i++) {
                double a = Math.PI * 2 * i / 12;
                P.puff(level, at.add(0, 0.2, 0), new Vec3(Math.cos(a) * 0.14, 0.02, Math.sin(a) * 0.14), 0xCCCCCC, 0.2f, 0.5f, 9, 0.5f);
            }
            P.vanilla(level, ParticleTypes.EXPLOSION, at.add(0, 0.5, 0), new Vec3(0.8, 0, 0));
            BlockParticleOption chips = new BlockParticleOption(ParticleTypes.BLOCK, ground(level, at));
            for (int i = 0; i < 8; i++) P.vanilla(level, chips, at.add(0, 0.2, 0), new Vec3(P.rs() * 0.2, 0.3, P.rs() * 0.2));
            ClientAnims.shakeNear(at, 16, 5, 0.4f);
        }
    }

    // ================================================================== GRAVITY FIELD

    public static final class GravityField extends Anim {
        private final Vec3 center;
        private final float radius;
        private final int rgb;
        private final int[] targets;

        public GravityField(ClientLevel level, Vec3 center, float radius, int rgb, int[] targets) {
            super(level);
            this.center = center;
            this.radius = Math.max(1.5f, radius);
            this.rgb = rgb;
            this.targets = targets;
        }

        @Override
        protected boolean step() {
            if (t < 6) {
                double r = radius * (t + 1) / 6.0;
                for (int i = 0; i < 34; i++) P.glow(level, center.add(P.unit().scale(r)), Vec3.ZERO, rgb, 0.18f, 5);
            } else {
                for (int i = 0; i < 3; i++) {
                    Vec3 p = center.add(P.unit().scale(radius * Math.cbrt(P.R.nextDouble())));
                    P.glow(level, p, new Vec3(0, 0.03, 0), rgb, 0.1f, 14);
                }
                if (t % 3 == 0) P.vanilla(level, ParticleTypes.END_ROD, center.add(P.unit().scale(radius * 0.8)), new Vec3(0, 0.03, 0));
            }
            for (int id : targets) {
                Entity e = ent(id);
                if (e == null || e.isRemoved()) continue;
                P.glow(level, body(e), new Vec3(0, 0.05, 0), rgb, 0.13f, 12);
                if (t % 5 == 0) {
                    Vec3 c = e.position().add(0, e.getBbHeight() * 0.5, 0);
                    for (int i = 0; i < 10; i++) {
                        double a = Math.PI * 2 * i / 10 + t * 0.2;
                        P.glow(level, c.add(Math.cos(a) * 0.6, 0, Math.sin(a) * 0.6), new Vec3(0, 0.04, 0), 0xFFFFFF, 0.08f, 8);
                    }
                }
            }
            return t >= 40;
        }
    }
}
