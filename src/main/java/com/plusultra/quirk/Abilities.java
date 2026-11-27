package com.plusultra.quirk;

import com.plusultra.config.PUConfig;
import com.plusultra.core.ModSounds;
import com.plusultra.item.HeroCostume;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** The actual effects of every Quirk ability. Everything here runs on the logical server. */
public final class Abilities {
    private Abilities() {}

    // ------------------------------------------------------------------ helpers

    public static ServerLevel level(ServerPlayer p) {
        return p.serverLevel();
    }

    public static boolean canHit(ServerPlayer p, Entity e) {
        if (!(e instanceof LivingEntity le) || e == p || !le.isAlive()) return false;
        if (le instanceof Player && !PUConfig.PVP.get()) return false;
        if (le instanceof TamableAnimal pet && pet.isOwnedBy(p)) return false;
        return true;
    }

    /** Living entities in front of the player within range; cosMin is the cosine of the half-angle. */
    public static List<LivingEntity> cone(ServerPlayer p, double range, double cosMin) {
        Vec3 eye = p.getEyePosition();
        Vec3 look = p.getLookAngle();
        List<LivingEntity> out = new ArrayList<>();
        for (LivingEntity e : p.level().getEntitiesOfClass(LivingEntity.class,
                p.getBoundingBox().inflate(range), x -> canHit(p, x))) {
            Vec3 to = e.getBoundingBox().getCenter().subtract(eye);
            double d = to.length();
            if (d > range) continue;
            if (d < 1.2 || to.normalize().dot(look) >= cosMin) out.add(e);
        }
        return out;
    }

    public static List<LivingEntity> around(ServerPlayer p, Vec3 center, double r) {
        return p.level().getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(r),
                x -> canHit(p, x) && x.position().distanceTo(center) <= r);
    }

    public static void hurt(ServerPlayer p, LivingEntity target, double amount) {
        double mult = PlayerData.get(p).damageMultiplier() * PUConfig.DAMAGE_MULT.get()
                * (HeroCostume.pieces(p) == 4 ? 1.1 : 1.0);
        target.hurt(p.damageSources().playerAttack(p), (float) (amount * mult));
    }

    public static void shove(LivingEntity target, Vec3 dir, double horizontal, double vertical) {
        target.push(dir.x * horizontal, vertical, dir.z * horizontal);
        target.hurtMarked = true;
    }

    public static Vec3 flat(Vec3 v) {
        return new Vec3(v.x, 0, v.z).normalize();
    }

    public static void dash(ServerPlayer p, double speed, double lift) {
        Vec3 l = p.getLookAngle();
        p.setDeltaMovement(l.x * speed, l.y * speed * 0.6 + lift, l.z * speed);
        p.hurtMarked = true;
        p.fallDistance = 0;
    }

    public static void particles(ServerPlayer p, ParticleOptions type, Vec3 at, int count, double spread, double speed) {
        level(p).sendParticles(type, at.x, at.y, at.z, count, spread, spread, spread, speed);
    }

    public static void sound(ServerPlayer p, SoundEvent s, float pitch) {
        p.level().playSound(null, p.getX(), p.getY(), p.getZ(), s, SoundSource.PLAYERS, 1.0f, pitch);
    }

    public static void effect(LivingEntity e, Holder<MobEffect> eff, int ticks, int amp) {
        e.addEffect(new MobEffectInstance(eff, ticks, amp));
    }

    /** Plays the explosion animation and applies its damage. */
    public static void blastAt(ServerPlayer p, Vec3 at, double radius, double damage) {
        Fx.play(FxType.MHA_EXPLOSION).a(at).f((float) radius).rgb(0xFF8C1A).send(level(p), at);
        blastDamage(p, at, radius, damage);
    }

    /** Sound, damage and optional block damage of an explosion. The visuals are sent separately. */
    public static void blastDamage(ServerPlayer p, Vec3 at, double radius, double damage) {
        ServerLevel l = level(p);
        l.playSound(null, at.x, at.y, at.z, SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.5f, 0.6f);
        l.playSound(null, at.x, at.y, at.z, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.6f, 1.6f);
        for (LivingEntity t : around(p, at, radius)) {
            hurt(p, t, damage);
            Vec3 away = t.position().subtract(at);
            shove(t, away.lengthSqr() < 1e-4 ? new Vec3(0, 0, 1) : away.normalize(), 1.0, 0.5);
        }
        if (PUConfig.BLOCK_DAMAGE.get()) {
            l.explode(p, at.x, at.y, at.z, (float) (radius * 0.6), Level.ExplosionInteraction.MOB);
        }
    }

    /** Cone of particles that widens with distance. */
    public static void coneFx(ServerPlayer p, ParticleOptions o, double range, int perStep) {
        Vec3 look = p.getLookAngle();
        for (int i = 1; i <= range; i++) {
            particles(p, o, p.getEyePosition().add(look.scale(i)), perStep, 0.12 + i * 0.07, 0.02);
        }
    }

    // ------------------------------------------------------------------ One For All

    public static void smash(ServerPlayer p, double power) {
        Vec3 look = p.getLookAngle();
        for (LivingEntity t : cone(p, 4.5, 0.6)) {
            hurt(p, t, 9 * power);
            shove(t, flat(look), 1.8, 0.45);
        }
        Vec3 pt = p.getEyePosition().add(look.scale(2.5));
        Fx.play(FxType.SMASH).src(p).a(pt).b(look).rgb(0x7CFC00).f(1.0f).send(p);
        sound(p, ModSounds.HEAVYPUNCH.get(), 1.0f);
    }

    public static void delawareLeap(ServerPlayer p, double power) {
        Fx.play(FxType.LEAP).src(p).rgb(0x7CFC00).send(p);
        p.setDeltaMovement(p.getLookAngle().x * 1.0, 1.4 + 0.05 * power, p.getLookAngle().z * 1.0);
        p.hurtMarked = true;
        p.fallDistance = 0;
        effect(p, MobEffects.SLOW_FALLING, 80, 0);
        sound(p, ModSounds.DASH.get(), 1.0f);
    }

    public static void fullCowl(ServerPlayer p, double power) {
        int t = 300;
        effect(p, MobEffects.DAMAGE_BOOST, t, 2);
        effect(p, MobEffects.MOVEMENT_SPEED, t, 1);
        effect(p, MobEffects.DAMAGE_RESISTANCE, t, 1);
        effect(p, MobEffects.JUMP, t, 1);
        Fx.play(FxType.LIGHTNING_BURST).a(p.position().add(0, 1, 0)).f(2.4f).rgb(0x7CFC00).n(8).send(p);
        Fx.startAura(p, t);
        sound(p, ModSounds.LIGHTNING_STRIKE.get(), 1.0f);
    }

    // ------------------------------------------------------------------ Half-Cold Half-Hot

    public static void flameBurst(ServerPlayer p, double power) {
        for (LivingEntity t : cone(p, 7, 0.75)) {
            hurt(p, t, 6 * power);
            t.setRemainingFireTicks(100);
        }
        Fx.play(FxType.FLAME_JET).src(p).a(p.getEyePosition()).b(p.getLookAngle()).f(7f).rgb(0xFF5A1F).m(0).send(p);
        sound(p, ModSounds.FIRE.get(), 1.0f);
    }

    public static void iceBurst(ServerPlayer p, double power) {
        for (LivingEntity t : cone(p, 7, 0.75)) {
            hurt(p, t, 5 * power);
            t.setTicksFrozen(200);
            effect(t, MobEffects.MOVEMENT_SLOWDOWN, 100, 2);
        }
        Fx.play(FxType.ICE_WAVE).a(p.position()).b(flat(p.getLookAngle())).f(7f).rgb(0x9FE8FF).send(p);
        sound(p, ModSounds.ICE.get(), 1.0f);
    }

    public static void flashfreezeHeatwave(ServerPlayer p, double power) {
        Vec3 c = p.position();
        for (LivingEntity t : around(p, c, 9)) {
            hurt(p, t, 10 * power);
            t.setRemainingFireTicks(120);
            t.setTicksFrozen(300);
            effect(t, MobEffects.MOVEMENT_SLOWDOWN, 120, 3);
            Vec3 away = t.position().subtract(c);
            shove(t, away.lengthSqr() < 1e-4 ? new Vec3(0, 0, 1) : away.normalize(), 1.2, 0.4);
        }
        Fx.play(FxType.DUAL_NOVA).a(c).f(9f).send(p);
        Fx.startAura(p, 100);
        sound(p, ModSounds.ICE2.get(), 1.0f);
        sound(p, ModSounds.FIRE.get(), 0.9f);
    }

    // ------------------------------------------------------------------ Explosion

    public static void blast(ServerPlayer p, double power) {
        Vec3 at = p.getEyePosition().add(p.getLookAngle().scale(3.0));
        Fx.play(FxType.PALM_CRACKLE).src(p).n(3).send(p);
        blastAt(p, at, 2.5, 7 * power);
    }

    public static void blastPropulsion(ServerPlayer p, double power) {
        Fx.play(FxType.BLAST_JET).src(p).b(p.getLookAngle()).n(7).send(p);
        dash(p, 1.8, 0.2);
        sound(p, SoundEvents.FIRECHARGE_USE, 1.2f);
    }

    public static void howitzerImpact(ServerPlayer p, double power) {
        Vec3 at = p.pick(20, 1.0f, false).getLocation();
        double damage = 14 * power;
        Fx.play(FxType.HOWITZER_WINDUP).src(p).a(at).f(5.5f).rgb(0xFF8C1A).n(8).send(p);
        // The damage lands when the wind-up animation ends and the explosion plays.
        Scheduler.schedule(8, () -> {
            if (p.isRemoved() || !p.isAlive()) return;
            blastDamage(p, at, 5.5, damage);
        });
    }

    // ------------------------------------------------------------------ Hardening

    public static void harden(ServerPlayer p, double power) {
        effect(p, MobEffects.DAMAGE_RESISTANCE, 400, 1);
        effect(p, MobEffects.ABSORPTION, 400, 1);
        effect(p, MobEffects.MOVEMENT_SLOWDOWN, 400, 0);
        Fx.play(FxType.SHARDS).a(p.position().add(0, 1, 0)).f(1.0f).rgb(0xC0392B).send(p);
        Fx.startAura(p, 400);
        sound(p, ModSounds.HEARDENING.get(), 1.0f);
    }

    public static void redRiotPunch(ServerPlayer p, double power) {
        Vec3 impact = p.getEyePosition().add(p.getLookAngle().scale(2.8));
        List<LivingEntity> hit = cone(p, 3.5, 0.8);
        if (!hit.isEmpty()) {
            LivingEntity t = hit.get(0);
            hurt(p, t, 12 * power);
            shove(t, flat(p.getLookAngle()), 1.2, 0.3);
            impact = t.getBoundingBox().getCenter();
        }
        Fx.play(FxType.SMASH).src(p).a(impact).b(p.getLookAngle()).rgb(0xC0392B).f(0.7f).send(p);
        sound(p, ModSounds.PUNCH.get(), 1.0f);
    }

    public static void unbreakable(ServerPlayer p, double power) {
        effect(p, MobEffects.DAMAGE_RESISTANCE, 200, 3);
        effect(p, MobEffects.DAMAGE_BOOST, 200, 1);
        effect(p, MobEffects.ABSORPTION, 200, 3);
        Vec3 c = p.position().add(0, 1, 0);
        Fx.play(FxType.SHARDS).a(c).f(1.6f).rgb(0xE74C3C).send(p);
        Fx.play(FxType.LIGHTNING_BURST).a(c).f(2.0f).rgb(0xE74C3C).n(6).send(p);
        Fx.startAura(p, 200);
        sound(p, ModSounds.KINZOKU1.get(), 1.0f);
    }

    // ------------------------------------------------------------------ Zero Gravity

    public static void floatTargets(ServerPlayer p, double power) {
        Vec3 c = p.getEyePosition().add(p.getLookAngle().scale(4));
        List<LivingEntity> hits = around(p, c, 5);
        for (LivingEntity t : hits) {
            effect(t, MobEffects.LEVITATION, 80, 0);
        }
        Fx.play(FxType.GRAVITY_FIELD).a(c).f(5f).rgb(0xFFB6C1).targets(hits).send(p);
        sound(p, ModSounds.EFFECT.get(), 1.0f);
    }

    public static void weightless(ServerPlayer p, double power) {
        effect(p, MobEffects.SLOW_FALLING, 200, 0);
        effect(p, MobEffects.JUMP, 200, 3);
        effect(p, MobEffects.MOVEMENT_SPEED, 200, 0);
        Fx.play(FxType.RING_BURST).a(p.position()).f(1.8f).rgb(0xFFB6C1).n(6).send(p);
        Fx.startAura(p, 200);
        sound(p, ModSounds.EFFECT.get(), 1.3f);
    }

    public static void meteorShower(ServerPlayer p, double power) {
        Vec3 c = p.position();
        for (LivingEntity t : around(p, c, 9)) {
            hurt(p, t, 4 * power);
            effect(t, MobEffects.LEVITATION, 100, 1);
            effect(t, MobEffects.GLOWING, 200, 0);
        }
        Fx.play(FxType.METEORS).a(c).f(9f).rgb(0xFFB6C1).send(p);
        sound(p, ModSounds.ROCK.get(), 1.0f);
    }

    // ------------------------------------------------------------------ Engine

    public static void reciproBurst(ServerPlayer p, double power) {
        effect(p, MobEffects.MOVEMENT_SPEED, 120, 3);
        Fx.play(FxType.SPEED_TRAIL).src(p).rgb(0x3498DB).n(60).send(p);
        Fx.startAura(p, 120);
        sound(p, ModSounds.DASH.get(), 1.3f);
    }

    public static void engineKick(ServerPlayer p, double power) {
        for (LivingEntity t : cone(p, 5, 0.7)) {
            hurt(p, t, 8 * power);
            shove(t, flat(p.getLookAngle()), 1.4, 0.35);
        }
        dash(p, 1.6, 0.1);
        Fx.play(FxType.SPEED_TRAIL).src(p).rgb(0x3498DB).n(10).send(p);
        Fx.play(FxType.RING_BURST).a(p.position()).f(2f).rgb(0x3498DB).n(6).send(p);
        sound(p, ModSounds.HEAVYSWING.get(), 1.0f);
    }

    public static void reciproExtend(ServerPlayer p, double power) {
        effect(p, MobEffects.MOVEMENT_SPEED, 240, 4);
        effect(p, MobEffects.JUMP, 240, 1);
        effect(p, MobEffects.DIG_SPEED, 240, 1);
        Fx.play(FxType.LIGHTNING_BURST).a(p.position().add(0, 1, 0)).f(2.4f).rgb(0x3498DB).n(8).send(p);
        Fx.play(FxType.SPEED_TRAIL).src(p).rgb(0x3498DB).n(80).m(1).send(p);
        Fx.startAura(p, 240);
        sound(p, ModSounds.LIGHTNING_STRIKE.get(), 1.2f);
    }

    // ------------------------------------------------------------------ shared by gadgets and Quirks

    /** Grey tape anchor. Used by the Grappling Hook gadget. */
    public static boolean grapple(ServerPlayer p, double range, double speed) {
        return grapple(p, range, speed, 0xDDDDDD, 4);
    }

    /**
     * Pulls the player toward the block they are looking at. Returns false if nothing is in range.
     * strandStyle: 4 = tape, 5 = whip (see AnimsMisc.Strand).
     */
    public static boolean grapple(ServerPlayer p, double range, double speed, int rgb, int strandStyle) {
        HitResult hit = p.pick(range, 1.0f, false);
        if (hit.getType() == HitResult.Type.MISS) return false;
        Vec3 to = hit.getLocation();
        Vec3 from = p.position().add(0, 1.0, 0);
        Vec3 d = to.subtract(from);
        double len = d.length();
        if (len < 1.5) return false;
        Vec3 v = d.normalize().scale(Math.min(speed, 0.6 + len * 0.08));
        p.setDeltaMovement(v.x, v.y + 0.25, v.z);
        p.hurtMarked = true;
        p.fallDistance = 0;
        Fx.play(FxType.STRAND).src(p).a(to).rgb(rgb).n(14).m(strandStyle).send(p);
        sound(p, SoundEvents.CROSSBOW_SHOOT, 0.9f);
        return true;
    }
}
