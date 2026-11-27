package com.plusultra.client.anim;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/** Tape and whip strands, wings and feathers, shadows, acid and sound. */
public final class AnimsMisc {
    private AnimsMisc() {}

    private static double ease(double u) {
        u = Math.max(0, Math.min(1, u));
        return 1 - (1 - u) * (1 - u);
    }

    // ================================================================== STRAND (tape / whip)

    /**
     * A ribbon shoots from the caster's hand to each target (or an anchor point). On a target it wraps in a
     * rising spiral and holds; at an anchor it becomes a taut rope; a whip lash snaps and retracts.
     * Styles: 0 tape shot (wraps the legs), 1 whip lash, 2 whip constrict (wraps), 3 tape cocoon (wraps fully),
     * 4 tape anchor (grapple), 5 whip anchor (grapple).
     */
    public static final class Strand extends Anim {
        private static final int FLY = 4;

        private final int src, rgb, hold, style, wrap;
        private final int[] targets;
        private final Vec3 anchor;
        private final double wrapFrac, turns;
        private final boolean attach;

        public Strand(ClientLevel level, int src, int[] targets, Vec3 anchor, int rgb, int hold, int style) {
            super(level);
            this.src = src;
            this.targets = targets;
            this.anchor = anchor;
            this.rgb = rgb;
            this.style = style;
            this.hold = Math.max(3, hold);
            this.wrap = switch (style) {
                case 0 -> 6;
                case 2 -> 8;
                case 3 -> 12;
                default -> 0;
            };
            this.wrapFrac = style == 0 ? 0.5 : 1.0;
            this.turns = style == 3 ? 5.0 : 3.0;
            this.attach = style == 4 || style == 5;
        }

        @Override
        protected boolean step() {
            Entity e = ent(src);
            if (e == null || e.isRemoved()) return true;
            Vec3 hand = hand(e);
            int total = FLY + wrap + hold;
            if (t >= total) {
                release();
                return true;
            }
            if (targets.length == 0) {
                anchorStep(hand);
            } else {
                for (int id : targets) {
                    Entity te = ent(id);
                    if (te == null || te.isRemoved()) continue;
                    targetStep(te, hand);
                }
            }
            return false;
        }

        // ---------------------------------------------------------------- anchor mode

        private void anchorStep(Vec3 hand) {
            if (t < FLY) {
                double u = (t + 1.0) / FLY;
                Vec3 head = hand.lerp(anchor, ease(u));
                ribbon(hand, head, 0.3 * (1 - u) + 0.04, 0.25 * (1 - u));
                headFx(head);
                return;
            }
            if (t == FLY) attachFx(anchor);
            if (attach) {
                ribbon(hand, anchor, 0.025, 0.0);
                for (int i = 0; i < 3; i++) {
                    double a = t * 0.8 + i * 2.09;
                    P.tape(level, anchor.add(Math.cos(a) * 0.25, (i - 1) * 0.1, Math.sin(a) * 0.25), rgb, 0.08f, 3);
                }
            } else {
                double u = (double) (t - FLY) / hold;
                Vec3 end = anchor.lerp(hand, ease(u));
                ribbon(hand, end, 0.15 * (1 - u) + 0.03, 0.0);
            }
        }

        // ---------------------------------------------------------------- target mode

        private void targetStep(Entity te, Vec3 hand) {
            Vec3 tc = te.getBoundingBox().getCenter();
            if (t < FLY) {
                double u = (t + 1.0) / FLY;
                Vec3 head = hand.lerp(tc, ease(u));
                ribbon(hand, head, (style == 1 || style == 2 ? 0.35 : 0.25) * (1 - u) + 0.04, 0.25 * (1 - u));
                headFx(head);
                return;
            }
            int wt = t - FLY;
            if (t == FLY) attachFx(tc);

            if (style == 1) { // snap and retract
                double u = Math.min(1.0, (double) wt / hold);
                Vec3 end = tc.lerp(hand, ease(u));
                ribbon(hand, end, 0.12 * (1 - u) + 0.02, 0.0);
                return;
            }
            if (wt < wrap) {
                double prog = Math.min(1.0, (wt + 1.0) / wrap) * wrapFrac;
                double prev = (double) wt / wrap * wrapFrac;
                int life = (FLY + wrap + hold) - t + 8;
                for (int k = 0; k <= 12; k++) {
                    double s = prev + (prog - prev) * k / 12.0;
                    P.tape(level, wrapPoint(te, s), rgb, 0.09f, life);
                }
                ribbon(hand, wrapPoint(te, prog), 0.05, 0.05);
            } else {
                ribbon(hand, tc, 0.03, 0.0);
                if (style == 3 && wt == wrap) cocoon(te);
                if (style == 2 && (wt - wrap) % 6 == 0) squeeze(te);
            }
        }

        private Vec3 wrapPoint(Entity te, double s) {
            double h = te.getBbHeight();
            double r = te.getBbWidth() * 0.55 + 0.12;
            double ang = (s / wrapFrac) * turns * Math.PI * 2 + te.getId() * 0.7;
            return new Vec3(te.getX() + Math.cos(ang) * r, te.getY() + s * h, te.getZ() + Math.sin(ang) * r);
        }

        private void cocoon(Entity te) {
            Vec3 c = te.getBoundingBox().getCenter();
            double r = te.getBbWidth() * 0.6 + 0.2;
            double h = te.getBbHeight() * 0.55;
            int life = hold + 6;
            for (int i = 0; i < 44; i++) {
                Vec3 u = P.unit();
                P.tape(level, c.add(u.x * r, u.y * h, u.z * r), rgb, 0.12f, life);
            }
            P.glowGrow(level, c, Vec3.ZERO, 0xFFFFFF, 0.3f, 1.0f, 4);
        }

        private void squeeze(Entity te) {
            Vec3 c = te.position().add(0, te.getBbHeight() * (0.2 + P.R.nextDouble() * 0.6), 0);
            double r = te.getBbWidth() * 0.9;
            for (int i = 0; i < 10; i++) {
                double a = Math.PI * 2 * i / 10;
                Vec3 pos = c.add(Math.cos(a) * r, 0, Math.sin(a) * r);
                P.glow(level, pos, new Vec3(-Math.cos(a) * 0.09, 0, -Math.sin(a) * 0.09), rgb, 0.14f, 5);
            }
            P.vanilla(level, ParticleTypes.SQUID_INK, c, P.rv(0.05));
        }

        // ---------------------------------------------------------------- drawing

        private void ribbon(Vec3 from, Vec3 to, double amp, double sag) {
            Vec3 d = to.subtract(from);
            double len = d.length();
            if (len < 0.1) return;
            int n = Math.max(3, (int) (len / 0.12));
            Vec3 dir = d.scale(1.0 / len);
            Vec3 p1 = P.perp(dir);
            Vec3 p2 = dir.cross(p1);
            for (int i = 0; i <= n; i++) {
                double s = i / (double) n;
                double env = Math.sin(s * Math.PI);
                Vec3 pt = from.add(d.scale(s))
                        .add(p1.scale(Math.sin(s * 9 + t * 1.3) * amp * env))
                        .add(p2.scale(Math.cos(s * 7 + t * 1.1) * amp * env))
                        .add(0, -sag * env, 0);
                P.tape(level, pt, rgb, 0.075f, 2);
            }
        }

        private void headFx(Vec3 head) {
            P.glow(level, head, Vec3.ZERO, 0xFFFFFF, 0.12f, 2);
            if (style == 1 || style == 2 || style == 5) P.glow(level, head, Vec3.ZERO, rgb, 0.22f, 3);
        }

        private void attachFx(Vec3 at) {
            P.glowGrow(level, at, Vec3.ZERO, 0xFFFFFF, 0.15f, 0.7f, 4);
            for (int i = 0; i < 8; i++) P.tapeV(level, at, P.unit().scale(0.2), rgb, 0.09f, 16, 0.05f);
            for (int i = 0; i < 6; i++) P.spark(level, at, P.unit().scale(0.2), rgb, 0.1f, 8);
            if (style == 1 || style == 2 || style == 5) {
                for (int i = 0; i < 8; i++) P.vanilla(level, ParticleTypes.SQUID_INK, at, P.unit().scale(0.15));
            }
            if (style == 1) P.vanilla(level, ParticleTypes.SWEEP_ATTACK, at, Vec3.ZERO);
        }

        private void release() {
            if (targets.length == 0) {
                if (attach) scraps(anchor);
                return;
            }
            for (int id : targets) {
                Entity te = ent(id);
                if (te != null && !te.isRemoved() && style != 1) scraps(te.getBoundingBox().getCenter());
            }
        }

        private void scraps(Vec3 at) {
            for (int i = 0; i < 6; i++) P.tapeV(level, at.add(P.rv(0.3)), P.rv(0.05), rgb, 0.09f, 22, 0.06f);
        }
    }

    // ================================================================== WINGS

    /** Big flapping wings made of glowing bones and feather tips. m = 1: soaring with an updraft. */
    public static final class Wings extends Anim {
        private final int src, rgb, ticks, style;

        public Wings(ClientLevel level, int src, int rgb, int ticks, int style) {
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
            Vec3 f = P.flat(e.getLookAngle());
            Vec3 back = f.scale(-1);
            Vec3 right = new Vec3(-f.z, 0, f.x);
            Vec3 base = e.position().add(0, e.getBbHeight() * 0.75, 0).add(back.scale(0.25));
            double flap = Math.toRadians(35 + 28 * Math.sin(t * 0.55));
            for (int side = -1; side <= 1; side += 2) {
                for (int i = 0; i < 5; i++) {
                    double a = flap - Math.toRadians(i * 13);
                    Vec3 dir = right.scale(side * Math.cos(a)).add(0, Math.sin(a), 0).add(back.scale(0.45)).normalize();
                    double len = 2.9 - 0.28 * i;
                    for (double s = 0.35 * len; s <= len; s += 0.32) {
                        Vec3 pt = base.add(dir.scale(s));
                        P.glow(level, pt, Vec3.ZERO, rgb, 0.1f, 2);
                        if (s > len - 0.35) P.feather(level, pt, Vec3.ZERO, 0.24f, 2, 0f);
                    }
                }
            }
            if (t % 3 == 0) {
                double side = P.R.nextBoolean() ? 1 : -1;
                Vec3 pt = base.add(right.scale(side * (0.8 + P.R.nextDouble() * 1.8))).add(0, P.rs() * 0.5, 0);
                P.feather(level, pt, back.scale(0.02).add(0, -0.02, 0), 0.2f, 30, 0.015f);
            }
            if (style == 1) {
                Vec3 feet = e.position();
                P.puff(level, feet.add(P.rs() * 0.5, 0.1, P.rs() * 0.5), new Vec3(0, 0.08, 0), 0xFFFFFF, 0.2f, 0.6f, 12, 0.5f);
                if (t % 2 == 0) {
                    double a = t * 0.6;
                    P.glow(level, feet.add(Math.cos(a) * 0.9, 0.2, Math.sin(a) * 0.9), new Vec3(0, 0.12, 0), rgb, 0.15f, 10);
                }
            }
            return false;
        }
    }

    // ================================================================== FEATHER VOLLEY

    private static final class Flyer {
        Vec3 pos;
        final Vec3 vel, dest;
        int life;

        Flyer(Vec3 pos, Vec3 vel, Vec3 dest, int life) {
            this.pos = pos;
            this.vel = vel;
            this.dest = dest;
            this.life = life;
        }
    }

    /** Feathers burst from the wings and streak to each target. */
    public static final class FeatherVolley extends Anim {
        private final int src, rgb, count;
        private final int[] targets;
        private final Vec3 fromFallback, dir;
        private final List<Flyer> flyers = new ArrayList<>();
        private int launched = 0;

        public FeatherVolley(ClientLevel level, int src, int[] targets, Vec3 from, Vec3 dir, int count, int rgb) {
            super(level);
            this.src = src;
            this.targets = targets;
            this.fromFallback = from;
            this.dir = dir.lengthSqr() < 1e-4 ? new Vec3(0, 0, 1) : dir.normalize();
            this.count = Math.max(1, count);
            this.rgb = rgb;
        }

        @Override
        protected boolean step() {
            if (t < 6) {
                int per = (count + 5) / 6;
                Entity e = ent(src);
                for (int k = 0; k < per && launched < count; k++, launched++) launch(e);
            }
            Iterator<Flyer> it = flyers.iterator();
            while (it.hasNext()) {
                Flyer f = it.next();
                f.pos = f.pos.add(f.vel);
                P.glow(level, f.pos, Vec3.ZERO, rgb, 0.13f, 5);
                P.glow(level, f.pos, Vec3.ZERO, 0xFFFFFF, 0.06f, 3);
                if (--f.life <= 0) {
                    impact(f.dest);
                    it.remove();
                }
            }
            return t >= 6 && flyers.isEmpty();
        }

        private void launch(Entity e) {
            Vec3 root;
            Vec3 right = new Vec3(1, 0, 0);
            if (e != null) {
                Vec3 f = P.flat(e.getLookAngle());
                right = new Vec3(-f.z, 0, f.x);
                root = e.position().add(0, e.getBbHeight() * 0.8, 0).subtract(f.scale(0.3));
            } else {
                root = fromFallback;
            }
            Vec3 origin = root.add(right.scale(P.rs() * 1.5)).add(0, P.rs() * 0.5 + 0.2, 0);
            Vec3 dest = null;
            if (targets.length > 0) {
                Entity te = ent(targets[launched % targets.length]);
                if (te != null && !te.isRemoved()) dest = te.getBoundingBox().getCenter().add(P.rv(0.2));
            }
            if (dest == null) dest = origin.add(dir.scale(13)).add(P.rv(1.2));
            double dist = origin.distanceTo(dest);
            int travel = Math.max(3, Math.min(9, (int) (dist / 2.4)));
            Vec3 vel = dest.subtract(origin).scale(1.0 / travel);
            P.feather(level, origin, vel, 0.3f, travel, 0f);
            P.glow(level, origin, Vec3.ZERO, rgb, 0.25f, 4);
            flyers.add(new Flyer(origin, vel, dest, travel));
        }

        private void impact(Vec3 at) {
            P.glowGrow(level, at, Vec3.ZERO, rgb, 0.2f, 0.7f, 5);
            for (int i = 0; i < 5; i++) P.vanilla(level, ParticleTypes.CRIT, at, P.unit().scale(0.3));
            for (int i = 0; i < 3; i++) P.feather(level, at, P.unit().scale(0.15), 0.18f, 16, 0.03f);
            for (int i = 0; i < 5; i++) P.spark(level, at, P.unit().scale(0.2), rgb, 0.1f, 9);
        }
    }

    // ================================================================== SHADOW ARM

    /** A thick tendril of shadow lunges out, strikes, and pulls back. Red eyes glow at its tip. */
    public static final class ShadowArm extends Anim {
        private final int src, rgb, ticks;
        private final Vec3 end;

        public ShadowArm(ClientLevel level, int src, Vec3 end, int rgb, int ticks) {
            super(level);
            this.src = src;
            this.end = end;
            this.rgb = rgb;
            this.ticks = Math.max(5, ticks);
        }

        @Override
        protected boolean step() {
            Entity e = ent(src);
            if (e == null || e.isRemoved() || t >= ticks) return true;
            Vec3 origin = e.position().add(0, e.getBbHeight() * 0.7, 0).add(rightOf(e).scale(0.3));
            int grow = 3;
            double u = t < grow ? (t + 1.0) / grow : t >= ticks - 2 ? (ticks - t) / 2.0 : 1.0;
            Vec3 head = origin.lerp(end, ease(u));
            Vec3 d = head.subtract(origin);
            double len = d.length();
            if (len > 0.1) {
                Vec3 dir = d.scale(1.0 / len);
                Vec3 p1 = P.perp(dir);
                Vec3 p2 = dir.cross(p1);
                int n = Math.max(3, (int) (len / 0.15));
                for (int k = 0; k < 3; k++) {
                    for (int i = 0; i <= n; i++) {
                        double s = i / (double) n;
                        double env = Math.sin(s * Math.PI) * 0.7 + 0.3 * s;
                        Vec3 pt = origin.add(d.scale(s))
                                .add(p1.scale(Math.sin(s * 10 + t * 1.7 + k * 2.1) * 0.22 * env))
                                .add(p2.scale(Math.cos(s * 8 + t * 1.5 + k * 2.1) * 0.22 * env));
                        P.glow(level, pt, Vec3.ZERO, rgb, 0.2f - k * 0.03f, 2);
                        if (i % 3 == 0) P.puff(level, pt, Vec3.ZERO, 0x14142B, 0.2f, 0.45f, 6, 0.5f);
                    }
                }
                Vec3 side = p1.scale(0.12);
                P.glow(level, head.add(side), Vec3.ZERO, 0xFF2020, 0.1f, 2);
                P.glow(level, head.subtract(side), Vec3.ZERO, 0xFF2020, 0.1f, 2);
            }
            if (t == grow) {
                for (int i = 0; i < 14; i++) P.vanilla(level, ParticleTypes.SQUID_INK, end, P.unit().scale(0.2));
                P.glowGrow(level, end, Vec3.ZERO, 0x6A3FA0, 0.2f, 0.9f, 5);
                for (int i = 0; i < 8; i++) P.spark(level, end, P.unit().scale(0.25), 0x6A3FA0, 0.1f, 10);
            }
            return false;
        }
    }

    // ================================================================== SHADOW ERUPT

    private static final class Column {
        final Vec3 base;
        final double height;
        int age = 0;
        double drawn = 0;

        Column(Vec3 base, double height) {
            this.base = base;
            this.height = height;
        }
    }

    public static final class ShadowErupt extends Anim {
        private final Vec3 center;
        private final float radius;
        private final int rgb;
        private final List<Column> cols = new ArrayList<>();

        public ShadowErupt(ClientLevel level, Vec3 center, float radius, int rgb) {
            super(level);
            this.center = center;
            this.radius = Math.max(2f, radius);
            this.rgb = rgb;
        }

        @Override
        protected boolean step() {
            if (t == 0) ClientAnims.shakeNear(center, 20, 12, 0.8f);
            if (t < 10) {
                for (int i = 0; i < 3; i++) {
                    double a = P.R.nextDouble() * Math.PI * 2;
                    double r = radius * Math.sqrt(P.R.nextDouble());
                    Vec3 pos = new Vec3(center.x + Math.cos(a) * r, 0, center.z + Math.sin(a) * r);
                    Vec3 base = new Vec3(pos.x, groundY(new Vec3(pos.x, center.y + 1.5, pos.z)), pos.z);
                    cols.add(new Column(base, 2.2 + P.R.nextDouble() * 2.0));
                }
            }
            if (t < 8) {
                double r = radius * (t + 1) / 8.0;
                double gy = groundY(center) + 0.1;
                int cnt = 14 + (int) (r * 5);
                for (int i = 0; i < cnt; i++) {
                    double a = Math.PI * 2 * i / cnt;
                    P.glow(level, new Vec3(center.x + Math.cos(a) * r, gy, center.z + Math.sin(a) * r), Vec3.ZERO, rgb, 0.2f, 4);
                    if (i % 4 == 0) P.vanilla(level, ParticleTypes.SQUID_INK, new Vec3(center.x + Math.cos(a) * r, gy, center.z + Math.sin(a) * r), new Vec3(0, 0.05, 0));
                }
            }
            for (Column c : cols) {
                c.age++;
                double reach = c.height * Math.min(1.0, c.age / 4.0);
                for (double h = c.drawn; h < reach; h += 0.25) {
                    double sway = Math.sin(h * 3 + c.base.x) * 0.15;
                    Vec3 p = c.base.add(sway, h, Math.cos(h * 3 + c.base.z) * 0.15);
                    float size = 0.3f - 0.12f * (float) (h / c.height);
                    P.glow(level, p, Vec3.ZERO, rgb, size, 14);
                    if (((int) (h * 4)) % 2 == 0) P.puff(level, p, new Vec3(0, 0.02, 0), 0x14142B, 0.25f, 0.55f, 12, 0.5f);
                }
                if (c.drawn < c.height && reach >= c.height) {
                    Vec3 top = c.base.add(0, c.height, 0);
                    P.glow(level, top.add(0.1, 0, 0), Vec3.ZERO, 0xFF2020, 0.1f, 12);
                    P.glow(level, top.add(-0.1, 0, 0), Vec3.ZERO, 0xFF2020, 0.1f, 12);
                }
                c.drawn = reach;
            }
            return t >= 16;
        }
    }

    // ================================================================== ACID POOL

    public static final class AcidPool extends Anim {
        private final Vec3 center;
        private final float radius;
        private final int rgb, ticks, style;

        public AcidPool(ClientLevel level, Vec3 center, float radius, int rgb, int ticks, int style) {
            super(level);
            this.center = center;
            this.radius = Math.max(1f, radius);
            this.rgb = rgb;
            this.ticks = Math.max(10, ticks);
            this.style = style;
        }

        private Vec3 inDisc(double y) {
            double a = P.R.nextDouble() * Math.PI * 2;
            double r = radius * Math.sqrt(P.R.nextDouble());
            return new Vec3(center.x + Math.cos(a) * r, y, center.z + Math.sin(a) * r);
        }

        @Override
        protected boolean step() {
            if (t >= ticks) return true;
            double gy = groundY(center) + 0.1;
            int bubbles = style == 1 ? 6 : 3;
            for (int i = 0; i < bubbles; i++) {
                P.glow(level, inDisc(gy), new Vec3(0, 0.04, 0), rgb, 0.12f + (float) P.R.nextDouble() * 0.08f, 12);
            }
            P.vanilla(level, ParticleTypes.ITEM_SLIME, inDisc(gy + 0.1), Vec3.ZERO);
            P.puff(level, inDisc(gy), new Vec3(0, 0.03, 0), 0xCFFF8A, 0.2f, 0.7f, 16, 0.25f);

            int phase = t % 10;
            if (phase < 6) {
                double r = radius * (0.3 + 0.7 * phase / 5.0);
                int cnt = 12 + (int) (radius * 5);
                for (int i = 0; i < cnt; i++) {
                    double a = Math.PI * 2 * i / cnt;
                    P.glow(level, new Vec3(center.x + Math.cos(a) * r, gy, center.z + Math.sin(a) * r), Vec3.ZERO, rgb, 0.16f, 3);
                }
            }
            if (style == 1 && t % 4 == 0) {
                double r = radius * 1.15 * ((t % 16) / 15.0);
                int cnt = 14 + (int) (radius * 5);
                for (int i = 0; i < cnt; i++) {
                    double a = Math.PI * 2 * i / cnt;
                    P.glow(level, new Vec3(center.x + Math.cos(a) * r, gy, center.z + Math.sin(a) * r), new Vec3(0, 0.03, 0), 0xE8FFB0, 0.14f, 4);
                }
            }
            return false;
        }
    }

    // ================================================================== SOUND WAVE

    /** Concentric pink rings travel forward, with floating notes. */
    public static final class SoundWave extends Anim {
        private final int src, rgb, ticks;
        private final Vec3 dir, fallback;
        private final float range;
        private Vec3 origin;
        private final List<Integer> rings = new ArrayList<>();

        public SoundWave(ClientLevel level, int src, Vec3 origin, Vec3 dir, float range, int rgb, int ticks) {
            super(level);
            this.src = src;
            this.fallback = origin;
            this.dir = dir.lengthSqr() < 1e-4 ? new Vec3(0, 0, 1) : dir.normalize();
            this.range = Math.max(2f, range);
            this.rgb = rgb;
            this.ticks = Math.max(4, ticks);
        }

        @Override
        protected boolean step() {
            if (t == 0) {
                Entity e = ent(src);
                origin = e != null ? e.getEyePosition().add(dir.scale(0.3)) : fallback;
                P.vanilla(level, ParticleTypes.SONIC_BOOM, origin.add(dir.scale(2)), Vec3.ZERO);
                ClientAnims.shakeNear(origin, 10, 4, 0.2f);
            }
            if (t < ticks && t % 2 == 0) rings.add(0);
            Vec3 p1 = P.perp(dir);
            Vec3 p2 = dir.cross(p1);
            Iterator<Integer> it = rings.iterator();
            List<Integer> next = new ArrayList<>();
            while (it.hasNext()) {
                int age = it.next();
                double dist = age * range / 8.0;
                double rad = 0.3 + age * 0.45;
                Vec3 c = origin.add(dir.scale(dist));
                for (int i = 0; i < 18; i++) {
                    double a = Math.PI * 2 * i / 18;
                    P.glow(level, c.add(p1.scale(Math.cos(a) * rad)).add(p2.scale(Math.sin(a) * rad)), Vec3.ZERO, rgb, 0.13f, 2);
                }
                if (dist < range) next.add(age + 1);
            }
            rings.clear();
            rings.addAll(next);
            if (t % 2 == 0) {
                Vec3 pos = origin.add(dir.scale(1 + P.R.nextDouble() * (range - 1))).add(P.rv(0.8));
                P.vanilla(level, ParticleTypes.NOTE, pos, new Vec3(P.R.nextDouble(), 0, 0));
            }
            return t >= ticks && rings.isEmpty();
        }
    }

    // ================================================================== RIPPLE

    /** Rings of sound expanding across the ground. m = 1 adds sonic booms and a camera shake (bass drop). */
    public static final class Ripple extends Anim {
        private final Vec3 center;
        private final float radius;
        private final int rgb, pulses, style;
        private final List<Integer> active = new ArrayList<>();
        private int started = 0;

        public Ripple(ClientLevel level, Vec3 center, float radius, int rgb, int pulses, int style) {
            super(level);
            this.center = center;
            this.radius = Math.max(2f, radius);
            this.rgb = rgb;
            this.pulses = Math.max(1, pulses);
            this.style = style;
        }

        @Override
        protected boolean step() {
            double gy = groundY(center) + 0.1;
            if (started < pulses && t % 6 == 0) {
                started++;
                active.add(0);
                if (style == 1) {
                    for (int i = 0; i < 6; i++) {
                        double a = Math.PI * 2 * i / 6;
                        P.vanilla(level, ParticleTypes.SONIC_BOOM, new Vec3(center.x + Math.cos(a) * 3, gy + 1.0, center.z + Math.sin(a) * 3), Vec3.ZERO);
                    }
                    ClientAnims.shakeNear(center, radius * 2 + 10, 8, 0.6f);
                }
            }
            List<Integer> next = new ArrayList<>();
            for (int age : active) {
                double r = (age + 1) * 1.3;
                if (r > radius) continue;
                int cnt = 14 + (int) (r * 5);
                for (int i = 0; i < cnt; i++) {
                    double a = Math.PI * 2 * i / cnt;
                    Vec3 pos = new Vec3(center.x + Math.cos(a) * r, gy, center.z + Math.sin(a) * r);
                    P.glow(level, pos, new Vec3(0, 0.02, 0), rgb, 0.2f, 3);
                    if (i % 2 == 0) P.puff(level, pos, new Vec3(0, 0.02, 0), rgb, 0.15f, 0.35f, 8, 0.35f);
                }
                next.add(age + 1);
            }
            active.clear();
            active.addAll(next);
            return started >= pulses && active.isEmpty();
        }
    }
}
