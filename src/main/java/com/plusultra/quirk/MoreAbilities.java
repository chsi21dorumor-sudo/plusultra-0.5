package com.plusultra.quirk;

import com.plusultra.core.ModSounds;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/** Abilities for Creation, Invisibility, Fierce Wings, Dark Shadow, Black Whip, Acid, Earphone Jack and Tape. */
public final class MoreAbilities {
    private MoreAbilities() {}

    // ------------------------------------------------------------------ helpers

    private static ParticleOptions iron() {
        return new BlockParticleOption(ParticleTypes.BLOCK, Blocks.IRON_BLOCK.defaultBlockState());
    }

    private static LivingEntity nearest(ServerPlayer p, double range, double cos) {
        LivingEntity best = null;
        double bd = Double.MAX_VALUE;
        for (LivingEntity e : Abilities.cone(p, range, cos)) {
            double d = e.distanceToSqr(p);
            if (d < bd) {
                bd = d;
                best = e;
            }
        }
        return best;
    }

    private static List<LivingEntity> nearestN(ServerPlayer p, double range, double cos, int n) {
        List<LivingEntity> all = new ArrayList<>(Abilities.cone(p, range, cos));
        all.sort(Comparator.comparingDouble(e -> e.distanceToSqr(p)));
        return all.size() > n ? all.subList(0, n) : all;
    }

    private static Vec3 center(LivingEntity e) {
        return e.getBoundingBox().getCenter();
    }

    private static void pull(LivingEntity t, ServerPlayer p, double strength) {
        Vec3 d = p.position().subtract(t.position());
        if (d.lengthSqr() < 1e-4) return;
        Vec3 n = d.normalize();
        t.push(n.x * strength, 0.25, n.z * strength);
        t.hurtMarked = true;
    }

    private static Vec3 right(ServerPlayer p) {
        Vec3 f = Abilities.flat(p.getLookAngle());
        return new Vec3(-f.z, 0, f.x);
    }

    // ------------------------------------------------------------------ Creation

    public static void forgeShield(ServerPlayer p, double power) {
        Abilities.effect(p, MobEffects.ABSORPTION, 300, 2);
        Abilities.effect(p, MobEffects.DAMAGE_RESISTANCE, 300, 1);
        Vec3 c = p.position().add(0, 1, 0);
        Fx.play(FxType.GATHER).a(c).f(2.4f).rgb(0xE67E22).n(8).send(p);
        Fx.play(FxType.RING_BURST).a(p.position()).f(1.8f).rgb(0xE67E22).n(6).send(p);
        Fx.startAura(p, 300);
        Abilities.sound(p, ModSounds.KINZOKU1.get(), 1.0f);
    }

    public static void creationCannon(ServerPlayer p, double power) {
        LivingEntity t = nearest(p, 26, 0.96);
        Vec3 from = p.getEyePosition().add(0, -0.3, 0);
        Vec3 to = t != null ? center(t) : p.pick(26, 1.0f, false).getLocation();
        Fx.play(FxType.PROJECTILE).a(from).b(to).rgb(0xE67E22).m(0).send(p);
        if (t != null) {
            Abilities.hurt(p, t, 10 * power);
            Abilities.shove(t, Abilities.flat(p.getLookAngle()), 1.5, 0.3);
        }
        Abilities.sound(p, ModSounds.LASER.get(), 1.0f);
    }

    public static void ironGuardians(ServerPlayer p, double power) {
        ServerLevel l = Abilities.level(p);
        Vec3 look = Abilities.flat(p.getLookAngle());
        Vec3 right = right(p);
        for (int s = -1; s <= 1; s += 2) {
            Vec3 pos = p.position().add(look.scale(2)).add(right.scale(2.0 * s));
            // The golems are forged out of converging light, then appear when it finishes.
            Fx.play(FxType.GATHER).a(pos.add(0, 1.2, 0)).f(1.8f).rgb(0xE67E22).n(8).send(l, pos);
            Scheduler.schedule(8, () -> spawnGolem(p, l, pos));
        }
        Abilities.sound(p, ModSounds.CHARGE.get(), 1.0f);
    }

    private static void spawnGolem(ServerPlayer p, ServerLevel l, Vec3 pos) {
        if (p.isRemoved()) return;
        IronGolem g = EntityType.IRON_GOLEM.create(l);
        if (g == null) return;
        g.moveTo(pos.x, p.getY(), pos.z, p.getYRot(), 0f);
        g.setPlayerCreated(true);
        l.addFreshEntity(g);
        Fx.play(FxType.RING_BURST).a(pos).f(2f).rgb(0xE67E22).n(6).send(l, pos);
        Scheduler.schedule(700, g::discard);
    }

    // ------------------------------------------------------------------ Invisibility

    public static void vanish(ServerPlayer p, double power) {
        Abilities.effect(p, MobEffects.INVISIBILITY, 200, 0);
        Abilities.effect(p, MobEffects.MOVEMENT_SPEED, 200, 1);
        Fx.play(FxType.SHIMMER).src(p).rgb(0xB0D0E8).n(40).send(p);
        Fx.play(FxType.RING_BURST).a(p.position()).f(1.6f).rgb(0xB0D0E8).n(6).send(p);
        Abilities.sound(p, ModSounds.EFFECT.get(), 1.5f);
    }

    public static void lightBendStrike(ServerPlayer p, double power) {
        LivingEntity t = nearest(p, 9, 0.7);
        Vec3 start = p.getEyePosition();
        if (t == null) {
            Fx.play(FxType.RING_BURST).a(p.position()).f(1.4f).rgb(0xB0D0E8).n(6).send(p);
            return;
        }
        Vec3 face = Abilities.flat(t.getLookAngle());
        Vec3 behind = t.position().subtract(face.scale(1.6));
        p.connection.teleport(behind.x, t.getY(), behind.z, t.getYRot(), 0f);
        Fx.play(FxType.PROJECTILE).a(start).b(center(t)).rgb(0xB0D0E8).m(2).n(2).send(p);
        Abilities.hurt(p, t, 11 * power);
        Abilities.sound(p, ModSounds.SWING.get(), 1.2f);
    }

    public static void mirage(ServerPlayer p, double power) {
        Abilities.effect(p, MobEffects.INVISIBILITY, 300, 0);
        Abilities.effect(p, MobEffects.MOVEMENT_SPEED, 300, 2);
        for (LivingEntity t : Abilities.around(p, p.position(), 9)) {
            Abilities.effect(t, MobEffects.BLINDNESS, 160, 0);
            Abilities.effect(t, MobEffects.MOVEMENT_SLOWDOWN, 100, 1);
        }
        Fx.play(FxType.DOME).a(p.position()).f(9f).rgb(0xB0D0E8).n(60).send(p);
        Fx.startAura(p, 300);
        Abilities.sound(p, ModSounds.GLASS.get(), 0.8f);
    }

    // ------------------------------------------------------------------ Fierce Wings

    private static void wings(ServerPlayer p, int rgb, int ticks, int style) {
        Fx.play(FxType.WINGS).src(p).rgb(rgb).n(ticks).m(style).send(p);
    }

    public static void windGale(ServerPlayer p, double power) {
        Vec3 look = p.getLookAngle();
        for (LivingEntity t : Abilities.cone(p, 6, 0.5)) {
            Abilities.hurt(p, t, 3 * power);
            Abilities.shove(t, Abilities.flat(look), 1.6, 0.5);
        }
        p.setDeltaMovement(look.x * 1.4, 0.9, look.z * 1.4);
        p.hurtMarked = true;
        p.fallDistance = 0;
        Abilities.effect(p, MobEffects.SLOW_FALLING, 100, 0);
        wings(p, 0xD9382A, 14, 0);
        Fx.play(FxType.FLAME_JET).src(p).a(p.getEyePosition()).b(look).f(6f).rgb(0xEEEEEE).m(1).send(p);
        Abilities.sound(p, ModSounds.TORNADO.get(), 1.0f);
    }

    public static void featherBarrage(ServerPlayer p, double power) {
        List<LivingEntity> targets = nearestN(p, 15, 0.85, 5);
        for (LivingEntity t : targets) {
            Abilities.hurt(p, t, 6 * power);
        }
        Fx.play(FxType.FEATHER_VOLLEY).src(p).targets(targets).a(p.getEyePosition()).b(p.getLookAngle())
                .n(14).rgb(0xD9382A).send(p);
        wings(p, 0xD9382A, 10, 0);
        Abilities.sound(p, ModSounds.WING.get(), 1.2f);
    }

    public static void soar(ServerPlayer p, double power) {
        for (LivingEntity t : Abilities.around(p, p.position(), 5)) {
            Abilities.hurt(p, t, 8 * power);
            Vec3 away = t.position().subtract(p.position());
            Abilities.shove(t, away.lengthSqr() < 1e-4 ? new Vec3(0, 0, 1) : away.normalize(), 1.4, 0.6);
        }
        Vec3 look = p.getLookAngle();
        p.setDeltaMovement(look.x * 0.8, 2.2, look.z * 0.8);
        p.hurtMarked = true;
        p.fallDistance = 0;
        Abilities.effect(p, MobEffects.SLOW_FALLING, 400, 0);
        Abilities.effect(p, MobEffects.MOVEMENT_SPEED, 400, 1);
        Abilities.effect(p, MobEffects.JUMP, 400, 2);
        wings(p, 0xD9382A, 40, 1);
        Fx.play(FxType.RING_BURST).a(p.position()).f(4f).rgb(0xFFFFFF).n(8).send(p);
        Fx.startAura(p, 300);
        Abilities.sound(p, ModSounds.WING.get(), 0.9f);
    }

    // ------------------------------------------------------------------ Dark Shadow

    public static void shadowJab(ServerPlayer p, double power) {
        List<LivingEntity> hits = Abilities.cone(p, 5.5, 0.7);
        for (LivingEntity t : hits) {
            Abilities.hurt(p, t, 9 * power);
            Abilities.effect(t, MobEffects.WITHER, 60, 0);
            Abilities.effect(t, MobEffects.MOVEMENT_SLOWDOWN, 60, 1);
            Abilities.shove(t, Abilities.flat(p.getLookAngle()), 1.2, 0.3);
        }
        if (hits.isEmpty()) {
            Fx.play(FxType.SHADOW_ARM).src(p).a(p.getEyePosition().add(p.getLookAngle().scale(5)))
                    .rgb(0x2C2C54).n(7).send(p);
        } else {
            for (int i = 0; i < Math.min(3, hits.size()); i++) {
                Fx.play(FxType.SHADOW_ARM).src(p).a(center(hits.get(i))).rgb(0x2C2C54).n(7).send(p);
            }
        }
        Abilities.sound(p, ModSounds.BITE_STRONG.get(), 1.0f);
    }

    public static void shadowVeil(ServerPlayer p, double power) {
        boolean dark = p.level().getMaxLocalRawBrightness(p.blockPosition()) < 8;
        Abilities.effect(p, MobEffects.DAMAGE_RESISTANCE, 300, dark ? 2 : 1);
        Abilities.effect(p, MobEffects.DAMAGE_BOOST, 300, dark ? 1 : 0);
        Abilities.effect(p, MobEffects.ABSORPTION, 300, 1);
        Fx.play(FxType.RING_BURST).a(p.position()).f(2.4f).rgb(0x2C2C54).n(6).send(p);
        Fx.startAura(p, 300);
        Abilities.sound(p, ModSounds.DECAY.get(), 1.0f);
    }

    public static void rampage(ServerPlayer p, double power) {
        Vec3 c = p.position();
        for (LivingEntity t : Abilities.around(p, c, 7)) {
            Abilities.hurt(p, t, 12 * power);
            Abilities.effect(t, MobEffects.WITHER, 100, 1);
            Vec3 away = t.position().subtract(c);
            Abilities.shove(t, away.lengthSqr() < 1e-4 ? new Vec3(0, 0, 1) : away.normalize(), 1.6, 0.5);
        }
        Abilities.effect(p, MobEffects.DAMAGE_BOOST, 300, 2);
        Abilities.effect(p, MobEffects.MOVEMENT_SPEED, 300, 1);
        Fx.play(FxType.SHADOW_ERUPT).a(c).f(7f).rgb(0x2C2C54).send(p);
        Fx.startAura(p, 300);
        Abilities.sound(p, ModSounds.BLOOD_1.get(), 1.0f);
    }

    // ------------------------------------------------------------------ Black Whip

    public static void lash(ServerPlayer p, double power) {
        LivingEntity t = nearest(p, 15, 0.92);
        Vec3 from = p.getEyePosition().add(0, -0.4, 0);
        if (t == null) {
            Vec3 end = from.add(p.getLookAngle().scale(10));
            Fx.play(FxType.STRAND).src(p).a(end).rgb(0x4A235A).n(6).m(1).send(p);
            return;
        }
        Fx.play(FxType.STRAND).src(p).targets(List.of(t)).rgb(0x4A235A).n(6).m(1).send(p);
        Abilities.hurt(p, t, 6 * power);
        pull(t, p, 1.5);
        Abilities.sound(p, ModSounds.MUCHI.get(), 1.0f);
    }

    public static void tetherSwing(ServerPlayer p, double power) {
        boolean hooked = Abilities.grapple(p, 30, 2.2, 0x4A235A, 5);
        if (!hooked) {
            Vec3 from = p.getEyePosition().add(0, -0.4, 0);
            Fx.play(FxType.STRAND).src(p).a(from.add(p.getLookAngle().scale(8))).rgb(0x4A235A).n(6).m(1).send(p);
        }
    }

    public static void constrict(ServerPlayer p, double power) {
        List<LivingEntity> hits = Abilities.around(p, p.position(), 8);
        for (LivingEntity t : hits) {
            Abilities.hurt(p, t, 8 * power);
            Abilities.effect(t, MobEffects.MOVEMENT_SLOWDOWN, 100, 3);
            Abilities.effect(t, MobEffects.WEAKNESS, 100, 0);
            pull(t, p, 1.1);
        }
        Fx.play(FxType.STRAND).src(p).targets(hits).rgb(0x4A235A).n(24).m(2).send(p);
        Fx.play(FxType.RING_BURST).a(p.position()).f(8f).rgb(0x4A235A).n(8).send(p);
        Abilities.sound(p, ModSounds.WHIP.get(), 1.0f);
    }

    // ------------------------------------------------------------------ Acid

    /** Damage pulses only. The bubbling visuals are one long client animation started by the caller. */
    private static void pool(ServerPlayer p, Vec3 c, double radius, double damage, int pulses) {
        if (pulses <= 0) return;
        for (LivingEntity t : Abilities.around(p, c, radius)) {
            Abilities.hurt(p, t, damage);
            Abilities.effect(t, MobEffects.POISON, 40, 0);
            Abilities.effect(t, MobEffects.MOVEMENT_SLOWDOWN, 30, 0);
        }
        Scheduler.schedule(10, () -> pool(p, c, radius, damage, pulses - 1));
    }

    public static void acidSpit(ServerPlayer p, double power) {
        LivingEntity t = nearest(p, 17, 0.93);
        Vec3 from = p.getEyePosition().add(0, -0.3, 0);
        Vec3 to = t != null ? center(t) : p.pick(17, 1.0f, false).getLocation();
        Fx.play(FxType.PROJECTILE).a(from).b(to).rgb(0xA6E22E).m(1).send(p);
        if (t != null) {
            Abilities.hurt(p, t, 6 * power);
            Abilities.effect(t, MobEffects.POISON, 100, 0);
            Abilities.effect(t, MobEffects.WEAKNESS, 100, 0);
        }
        Abilities.sound(p, ModSounds.WATER.get(), 1.2f);
    }

    public static void acidPool(ServerPlayer p, double power) {
        Vec3 c = p.position().add(Abilities.flat(p.getLookAngle()).scale(4));
        Abilities.sound(p, ModSounds.WATER.get(), 0.8f);
        Fx.play(FxType.ACID_POOL).a(c).f(3f).rgb(0xA6E22E).n(100).m(0).send(p);
        pool(p, c, 3.0, 2.0 * power, 10);
    }

    public static void acidFlood(ServerPlayer p, double power) {
        Abilities.effect(p, MobEffects.MOVEMENT_SPEED, 300, 1);
        Abilities.effect(p, MobEffects.DAMAGE_RESISTANCE, 160, 0);
        Abilities.sound(p, ModSounds.WATER.get(), 0.6f);
        Fx.play(FxType.ACID_POOL).a(p.position()).f(6.5f).rgb(0xA6E22E).n(160).m(1).send(p);
        Fx.startAura(p, 200);
        pool(p, p.position(), 6.5, 3.0 * power, 16);
    }

    // ------------------------------------------------------------------ Earphone Jack

    public static void heartbeatPulse(ServerPlayer p, double power) {
        Vec3 look = p.getLookAngle();
        for (LivingEntity t : Abilities.cone(p, 9, 0.8)) {
            Abilities.hurt(p, t, 6 * power);
            Abilities.effect(t, MobEffects.CONFUSION, 100, 0);
            Abilities.shove(t, Abilities.flat(look), 1.3, 0.3);
        }
        Fx.play(FxType.SOUND_WAVE).src(p).a(p.getEyePosition()).b(look).f(9f).rgb(0xFF69B4).n(8).send(p);
        Abilities.sound(p, ModSounds.ONPA.get(), 1.0f);
    }

    public static void soundSense(ServerPlayer p, double power) {
        ServerLevel l = Abilities.level(p);
        Abilities.effect(p, MobEffects.NIGHT_VISION, 400, 0);
        for (LivingEntity e : l.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(24),
                x -> x != p && x.isAlive())) {
            Abilities.effect(e, MobEffects.GLOWING, 200, 0);
        }
        Fx.play(FxType.RIPPLE).a(p.position()).f(24f).rgb(0xFF69B4).n(3).m(0).send(p);
        Abilities.sound(p, ModSounds.ONPA.get(), 1.4f);
    }

    public static void heartbeatFuzz(ServerPlayer p, double power) {
        Vec3 c = p.position();
        for (LivingEntity t : Abilities.around(p, c, 10)) {
            Abilities.hurt(p, t, 10 * power);
            Abilities.effect(t, MobEffects.CONFUSION, 160, 0);
            Abilities.effect(t, MobEffects.WEAKNESS, 160, 0);
            Vec3 away = t.position().subtract(c);
            Abilities.shove(t, away.lengthSqr() < 1e-4 ? new Vec3(0, 0, 1) : away.normalize(), 1.6, 0.5);
        }
        Fx.play(FxType.RIPPLE).a(c).f(10f).rgb(0xFF69B4).n(3).m(1).send(p);
        Abilities.sound(p, ModSounds.ONPA.get(), 0.7f);
    }

    // ------------------------------------------------------------------ Tape

    public static void tapeShot(ServerPlayer p, double power) {
        LivingEntity t = nearest(p, 15, 0.92);
        Vec3 from = p.getEyePosition().add(0, -0.4, 0);
        if (t != null) {
            Abilities.hurt(p, t, 4 * power);
            Abilities.effect(t, MobEffects.MOVEMENT_SLOWDOWN, 80, 4);
            pull(t, p, 0.7);
            Fx.play(FxType.STRAND).src(p).targets(List.of(t)).rgb(0xD0D0D0).n(30).m(0).send(p);
        } else {
            Fx.play(FxType.STRAND).src(p).a(from.add(p.getLookAngle().scale(10))).rgb(0xD0D0D0).n(6).m(0).send(p);
        }
        Abilities.sound(p, ModSounds.HAJIKU.get(), 1.0f);
    }

    public static void tapeSwing(ServerPlayer p, double power) {
        boolean hooked = Abilities.grapple(p, 28, 2.0, 0xD0D0D0, 4);
        if (!hooked) {
            Vec3 from = p.getEyePosition().add(0, -0.4, 0);
            Fx.play(FxType.STRAND).src(p).a(from.add(p.getLookAngle().scale(8))).rgb(0xD0D0D0).n(6).m(0).send(p);
        }
    }

    public static void tapeCocoon(ServerPlayer p, double power) {
        List<LivingEntity> hits = Abilities.around(p, p.position(), 7.5);
        for (LivingEntity t : hits) {
            Abilities.hurt(p, t, 5 * power);
            Abilities.effect(t, MobEffects.MOVEMENT_SLOWDOWN, 100, 7);
            Abilities.effect(t, MobEffects.WEAKNESS, 100, 1);
        }
        Abilities.effect(p, MobEffects.DAMAGE_RESISTANCE, 100, 0);
        Fx.play(FxType.STRAND).src(p).targets(hits).rgb(0xD0D0D0).n(40).m(3).send(p);
        Fx.play(FxType.RING_BURST).a(p.position()).f(7.5f).rgb(0xD0D0D0).n(8).send(p);
        Abilities.sound(p, ModSounds.MUCHI_SWING.get(), 1.0f);
    }
}
