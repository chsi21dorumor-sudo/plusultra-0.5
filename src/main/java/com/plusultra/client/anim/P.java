package com.plusultra.client.anim;

import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/** Particle spawning and small vector helpers used by every animation. All of this runs on the client. */
public final class P {
    public static final Random R = new Random();

    private P() {}

    // ------------------------------------------------------------------ randomness and vectors

    /** Random number in [-1, 1]. */
    public static double rs() {
        return R.nextDouble() * 2 - 1;
    }

    public static Vec3 rv(double s) {
        return new Vec3(rs() * s, rs() * s, rs() * s);
    }

    public static Vec3 unit() {
        Vec3 v;
        do {
            v = new Vec3(rs(), rs(), rs());
        } while (v.lengthSqr() < 0.01 || v.lengthSqr() > 1.0);
        return v.normalize();
    }

    /** A horizontal unit vector perpendicular-ish to d (falls back to X when d is vertical). */
    public static Vec3 perp(Vec3 d) {
        Vec3 up = Math.abs(d.y) > 0.9 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        return d.cross(up).normalize();
    }

    public static Vec3 flat(Vec3 v) {
        return new Vec3(v.x, 0, v.z).normalize();
    }

    /** Point on a Fibonacci sphere, direction only. */
    public static Vec3 fib(int i, int n, double rot) {
        double y = 1 - 2 * (i + 0.5) / n;
        double rad = Math.sqrt(Math.max(0, 1 - y * y));
        double phi = i * 2.399963 + rot;
        return new Vec3(Math.cos(phi) * rad, y, Math.sin(phi) * rad);
    }

    public static int mix(int c1, int c2, double t) {
        t = Math.max(0, Math.min(1, t));
        int r = (int) (((c1 >> 16) & 255) * (1 - t) + ((c2 >> 16) & 255) * t);
        int g = (int) (((c1 >> 8) & 255) * (1 - t) + ((c2 >> 8) & 255) * t);
        int b = (int) ((c1 & 255) * (1 - t) + (c2 & 255) * t);
        return (r << 16) | (g << 8) | b;
    }

    // ------------------------------------------------------------------ custom particles

    private static void add(Particle p) {
        Minecraft.getInstance().particleEngine.add(p);
    }

    /** Soft round glow that shrinks to nothing. */
    public static void glow(ClientLevel l, Vec3 p, Vec3 v, int rgb, float size, int life) {
        if (FxSprites.GLOW == null) return;
        add(new FxParticle(l, p.x, p.y, p.z, v.x, v.y, v.z, FxSprites.GLOW, rgb, size, 0f, life, 0f, 0.9f, 0f, 1f));
    }

    /** Soft round glow that grows (flashes, shock fronts). */
    public static void glowGrow(ClientLevel l, Vec3 p, Vec3 v, int rgb, float size0, float size1, int life) {
        if (FxSprites.GLOW == null) return;
        add(new FxParticle(l, p.x, p.y, p.z, v.x, v.y, v.z, FxSprites.GLOW, rgb, size0, size1, life, 0f, 0.9f, 0f, 1f));
    }

    /** Four-pointed spark with gravity. */
    public static void spark(ClientLevel l, Vec3 p, Vec3 v, int rgb, float size, int life) {
        if (FxSprites.SPARK == null) return;
        add(new FxParticle(l, p.x, p.y, p.z, v.x, v.y, v.z, FxSprites.SPARK, rgb, size, size * 0.3f, life,
                0.25f, 0.95f, (float) rs() * 0.2f, 1f));
    }

    /** Red-and-white feather, tumbling. */
    public static void feather(ClientLevel l, Vec3 p, Vec3 v, float size, int life, float gravity) {
        if (FxSprites.FEATHER == null) return;
        add(new FxParticle(l, p.x, p.y, p.z, v.x, v.y, v.z, FxSprites.FEATHER, 0xFFFFFF, size, size, life,
                gravity, 1.0f, (float) rs() * 0.25f, 1f));
    }

    /** Strip of tape (also used for whip tendrils, tinted dark). Stays where it is. */
    public static void tape(ClientLevel l, Vec3 p, int rgb, float size, int life) {
        if (FxSprites.TAPE == null) return;
        add(new FxParticle(l, p.x, p.y, p.z, 0, 0, 0, FxSprites.TAPE, rgb, size, size, life, 0f, 1f, 0f, 1f));
    }

    /** Tape scrap that flies and falls. */
    public static void tapeV(ClientLevel l, Vec3 p, Vec3 v, int rgb, float size, int life, float gravity) {
        if (FxSprites.TAPE == null) return;
        add(new FxParticle(l, p.x, p.y, p.z, v.x, v.y, v.z, FxSprites.TAPE, rgb, size, size, life, gravity,
                0.96f, (float) rs() * 0.3f, 1f));
    }

    /** Soft smoke or cloud puff that grows and drifts up. */
    public static void puff(ClientLevel l, Vec3 p, Vec3 v, int rgb, float size0, float size1, int life, float alpha) {
        if (FxSprites.PUFF == null) return;
        add(new FxParticle(l, p.x, p.y, p.z, v.x, v.y, v.z, FxSprites.PUFF, rgb, size0, size1, life,
                -0.004f, 0.9f, (float) rs() * 0.06f, alpha));
    }

    // ------------------------------------------------------------------ vanilla particles

    public static void vanilla(ClientLevel l, ParticleOptions o, Vec3 p, Vec3 v) {
        l.addParticle(o, p.x, p.y, p.z, v.x, v.y, v.z);
    }

    public static void dust(ClientLevel l, int rgb, float size, Vec3 p, Vec3 v) {
        l.addParticle(new DustParticleOptions(new Vector3f(((rgb >> 16) & 255) / 255f,
                ((rgb >> 8) & 255) / 255f, (rgb & 255) / 255f), size), p.x, p.y, p.z, v.x, v.y, v.z);
    }

    // ------------------------------------------------------------------ shapes

    /** A short, glowing straight line that lives for {@code life} ticks. */
    public static void line(ClientLevel l, Vec3 a, Vec3 b, int rgb, int life) {
        Vec3 d = b.subtract(a);
        double len = d.length();
        int n = Math.max(1, (int) (len / 0.2));
        for (int k = 0; k <= n; k++) {
            Vec3 pt = a.add(d.scale(k / (double) n));
            glow(l, pt, Vec3.ZERO, 0xFFFFFF, 0.07f, life);
            if (k % 2 == 0) glow(l, pt, Vec3.ZERO, rgb, 0.17f, life + 1);
        }
    }

    /** Jagged lightning bolt with a white core and a colored halo. */
    public static void bolt(ClientLevel l, Vec3 from, Vec3 to, int rgb, int life, double jitter) {
        Vec3 d = to.subtract(from);
        double len = d.length();
        if (len < 0.05) return;
        int segs = Math.max(2, (int) (len / 0.6));
        Vec3 dir = d.scale(1.0 / len);
        Vec3 p1 = perp(dir);
        Vec3 p2 = dir.cross(p1);
        Vec3 prev = from;
        for (int i = 1; i <= segs; i++) {
            Vec3 pt = i == segs ? to
                    : from.add(dir.scale(len * i / segs)).add(p1.scale(rs() * jitter)).add(p2.scale(rs() * jitter));
            line(l, prev, pt, rgb, life);
            prev = pt;
        }
    }
}
