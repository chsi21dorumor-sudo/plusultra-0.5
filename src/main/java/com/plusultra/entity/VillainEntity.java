package com.plusultra.entity;

import com.plusultra.quirk.Fx;
import com.plusultra.quirk.Scheduler;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * One class for the whole Villain Alliance. {@link Kind} decides stats, look and signature attack:
 * Decay Lord (shockwave), Nomu (leap slam + regeneration), Hero Killer (backstab + paralysis),
 * Blue Flame (flame cone) and the Thug minion (no special, no boss bar).
 */
public class VillainEntity extends Monster {

    public enum Kind {
        //          texture        boss   scale health dmg  speed armor kbRes xp  bar
        DECAY_LORD("villain",      true,  1.00f, 140, 7,  0.30, 6, 0.6, 40, BossEvent.BossBarColor.PURPLE),
        NOMU("nomu",               true,  1.35f, 300, 11, 0.26, 8, 0.9, 70, BossEvent.BossBarColor.WHITE),
        HERO_KILLER("hero_killer", true,  1.00f, 150, 9,  0.36, 2, 0.4, 50, BossEvent.BossBarColor.PINK),
        BLUE_FLAME("blue_flame",   true,  1.05f, 170, 8,  0.30, 4, 0.5, 55, BossEvent.BossBarColor.BLUE),
        THUG("thug",               false, 0.95f, 40,  5,  0.30, 1, 0.0, 8,  BossEvent.BossBarColor.WHITE);

        public final String texture;
        public final boolean boss;
        public final float scale;
        public final double health;
        public final double damage;
        public final double speed;
        public final double armor;
        public final double knockbackResistance;
        public final int xp;
        public final BossEvent.BossBarColor barColor;

        Kind(String texture, boolean boss, float scale, double health, double damage, double speed, double armor,
             double knockbackResistance, int xp, BossEvent.BossBarColor barColor) {
            this.texture = texture;
            this.boss = boss;
            this.scale = scale;
            this.health = health;
            this.damage = damage;
            this.speed = speed;
            this.armor = armor;
            this.knockbackResistance = knockbackResistance;
            this.xp = xp;
            this.barColor = barColor;
        }
    }

    private final Kind kind;
    private final ServerBossEvent bossEvent;
    private boolean wasEnraged = false;

    public VillainEntity(EntityType<? extends VillainEntity> type, Level level, Kind kind) {
        super(type, level);
        this.kind = kind;
        this.xpReward = kind.xp;
        this.bossEvent = new ServerBossEvent(getDisplayName(), kind.barColor, BossEvent.BossBarOverlay.PROGRESS);
    }

    public Kind kind() {
        return kind;
    }

    public static AttributeSupplier.Builder createAttributes(Kind k) {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, k.health)
                .add(Attributes.MOVEMENT_SPEED, k.speed)
                .add(Attributes.ATTACK_DAMAGE, k.damage)
                .add(Attributes.ARMOR, k.armor)
                .add(Attributes.KNOCKBACK_RESISTANCE, k.knockbackResistance)
                .add(Attributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, kind == Kind.HERO_KILLER ? 1.25 : 1.1, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (!(level() instanceof ServerLevel sl)) return;

        boolean enraged = false;
        if (kind.boss) {
            bossEvent.setProgress(getHealth() / getMaxHealth());
            enraged = getHealth() < getMaxHealth() * 0.5f;
            if (enraged && !wasEnraged) {
                wasEnraged = true;
                bossEvent.setColor(BossEvent.BossBarColor.RED);
                addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, Integer.MAX_VALUE, 0, false, false));
                addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, Integer.MAX_VALUE, 0, false, false));
                playSound(SoundEvents.ENDER_DRAGON_GROWL, 2.0f, 0.7f);
                Fx.sphere(sl, Fx.dust(0xB22222, 2.0f), position().add(0, 1, 0), 1.8, 60);
            }
        }

        aura(sl, enraged);

        LivingEntity target = getTarget();
        if (target == null || !target.isAlive()) return;
        switch (kind) {
            case DECAY_LORD -> decay(sl, enraged);
            case NOMU -> nomu(sl, target, enraged);
            case HERO_KILLER -> heroKiller(sl, target, enraged);
            case BLUE_FLAME -> blueFlame(sl, target, enraged);
            case THUG -> { }
        }
    }

    // ------------------------------------------------------------------ ambient visuals

    private void aura(ServerLevel sl, boolean enraged) {
        if (tickCount % 4 != 0) return;
        switch (kind) {
            case DECAY_LORD -> {
                sl.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 1.0, getZ(), 3, 0.4, 0.8, 0.4, 0.01);
                if (enraged) sl.sendParticles(Fx.dust(0xB22222, 1.4f), getX(), getY() + 1.0, getZ(), 3, 0.4, 0.8, 0.4, 0);
            }
            case NOMU -> {
                sl.sendParticles(Fx.dust(0x555566, 1.2f), getX(), getY() + 1.4, getZ(), 2, 0.5, 1.0, 0.5, 0);
                if (enraged) sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, getX(), getY() + 2.4, getZ(), 2, 0.3, 0.3, 0.3, 0);
            }
            case HERO_KILLER -> {
                if (enraged) sl.sendParticles(Fx.dust(0xB22222, 1.0f), getX(), getY() + 1.0, getZ(), 2, 0.3, 0.8, 0.3, 0);
            }
            case BLUE_FLAME -> sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, getX(), getY() + 1.0, getZ(), 2, 0.35, 0.8, 0.35, 0.01);
            case THUG -> { }
        }
    }

    // ------------------------------------------------------------------ signature attacks

    /** Decay Lord: purple shockwave around himself every 6s (4s enraged). */
    private void decay(ServerLevel sl, boolean enraged) {
        if (tickCount % (enraged ? 80 : 120) != 0 || distanceToSqr(getTarget()) > 12 * 12) return;
        Vec3 c = position();
        sl.sendParticles(ParticleTypes.LARGE_SMOKE, c.x, c.y + 0.5, c.z, 40, 2.0, 0.3, 2.0, 0.02);
        for (int r = 1; r <= 7; r += 2) {
            Fx.ring(sl, Fx.dust(0x6A0DAD, 1.6f), c.add(0, 0.2, 0), r, 12 + r * 5);
            Fx.ring(sl, ParticleTypes.SOUL_FIRE_FLAME, c.add(0, 0.4, 0), r, 8 + r * 3);
        }
        Fx.spiral(sl, ParticleTypes.SQUID_INK, c, 3, 1.0, 3, 30);
        sl.playSound(null, c.x, c.y, c.z, SoundEvents.WITHER_SHOOT, SoundSource.HOSTILE, 1.0f, 0.6f);
        for (LivingEntity p : victims(sl, 7)) {
            p.hurt(damageSources().mobAttack(this), enraged ? 8.0f : 6.0f);
            p.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0));
            knock(p, c, 1.2, 0.4);
        }
    }

    /** Nomu: leaps at the target and slams the ground; regenerates when enraged. */
    private void nomu(ServerLevel sl, LivingEntity target, boolean enraged) {
        if (enraged && tickCount % 40 == 0) heal(3.0f);
        if (tickCount % (enraged ? 70 : 100) != 0 || distanceToSqr(target) > 14 * 14) return;
        Vec3 dir = target.position().subtract(position());
        Vec3 flat = new Vec3(dir.x, 0, dir.z);
        if (flat.lengthSqr() > 1e-4) {
            flat = flat.normalize();
            setDeltaMovement(flat.x * 0.9, 0.75, flat.z * 0.9);
            hurtMarked = true;
        }
        playSound(SoundEvents.RAVAGER_ROAR, 1.5f, 0.6f);
        Scheduler.schedule(14, () -> {
            if (!isAlive() || !(level() instanceof ServerLevel l2)) return;
            Vec3 c = position();
            Fx.burst(l2, new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                    c.add(0, 0.3, 0), 60, 2.5, 0.2);
            l2.sendParticles(ParticleTypes.EXPLOSION_EMITTER, c.x, c.y, c.z, 1, 0, 0, 0, 0);
            for (int r = 2; r <= 6; r += 2) {
                Fx.ring(l2, Fx.dust(0x666677, 1.8f), c.add(0, 0.2, 0), r, 14 + r * 5);
                Fx.ring(l2, ParticleTypes.CLOUD, c.add(0, 0.3, 0), r, 8 + r * 3);
            }
            l2.playSound(null, c.x, c.y, c.z, SoundEvents.IRON_GOLEM_DAMAGE, SoundSource.HOSTILE, 1.2f, 0.7f);
            for (LivingEntity p : victims(l2, 6)) {
                p.hurt(damageSources().mobAttack(this), enraged ? 12.0f : 9.0f);
                knock(p, c, 1.0, 0.9);
            }
        });
    }

    /** Hero Killer: blinks behind the target and slashes, paralysing them briefly. */
    private void heroKiller(ServerLevel sl, LivingEntity target, boolean enraged) {
        if (tickCount % (enraged ? 60 : 90) != 0 || distanceToSqr(target) > 18 * 18 || distanceToSqr(target) < 3 * 3) return;
        Vec3 face = new Vec3(target.getLookAngle().x, 0, target.getLookAngle().z);
        if (face.lengthSqr() < 1e-4) face = new Vec3(0, 0, 1);
        Vec3 behind = target.position().subtract(face.normalize().scale(1.6));
        Fx.burst(sl, Fx.dust(0x151018, 1.6f), position().add(0, 1, 0), 24, 0.4, 0.05);
        Fx.burst(sl, Fx.dust(0xC0222A, 1.2f), position().add(0, 1, 0), 10, 0.3, 0.05);
        float yaw = (float) (Math.atan2(target.getZ() - behind.z, target.getX() - behind.x) * 57.29578) - 90f;
        moveTo(behind.x, target.getY(), behind.z, yaw, 0f);
        getNavigation().stop();
        playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0f, 1.5f);
        Scheduler.schedule(6, () -> {
            if (!isAlive() || !target.isAlive() || distanceToSqr(target) > 5 * 5 || !(level() instanceof ServerLevel l2)) return;
            target.hurt(damageSources().mobAttack(this), enraged ? 10.0f : 7.0f);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 5));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 1));
            Vec3 mid = target.getBoundingBox().getCenter();
            Fx.burst(l2, ParticleTypes.SWEEP_ATTACK, mid, 3, 0.4, 0);
            Fx.burst(l2, Fx.dust(0xC0222A, 1.5f), mid, 20, 0.4, 0.1);
            Fx.beam(l2, Fx.dust(0xDDDDDD, 1.0f), mid.add(-0.8, 0.8, -0.8), mid.add(0.8, -0.6, 0.8), 0.2);
            l2.playSound(null, mid.x, mid.y, mid.z, SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 1.0f, 1.2f);
            if (target instanceof ServerPlayer sp) {
                sp.displayClientMessage(Component.literal("Bloodcurdle - you are paralyzed!")
                        .withStyle(net.minecraft.ChatFormatting.RED), true);
            }
        });
    }

    /** Blue Flame: a cone of blue fire toward the target; enraged adds a ring of fire around himself. */
    private void blueFlame(ServerLevel sl, LivingEntity target, boolean enraged) {
        if (tickCount % (enraged ? 60 : 100) != 0 || distanceToSqr(target) > 16 * 16) return;
        Vec3 from = getEyePosition();
        Vec3 look = target.getBoundingBox().getCenter().subtract(from).normalize();
        for (int i = 1; i <= 13; i++) {
            Vec3 at = from.add(look.scale(i));
            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, at.x, at.y, at.z, 6, 0.15 + i * 0.06, 0.15 + i * 0.06, 0.15 + i * 0.06, 0.02);
            sl.sendParticles(Fx.dust(0x3A7BFF, 1.6f), at.x, at.y, at.z, 3, 0.2 + i * 0.05, 0.2 + i * 0.05, 0.2 + i * 0.05, 0);
        }
        playSound(SoundEvents.BLAZE_SHOOT, 1.4f, 0.7f);
        for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(14),
                x -> x != this && x.isAlive() && !(x instanceof VillainEntity))) {
            Vec3 to = e.getBoundingBox().getCenter().subtract(from);
            double d = to.length();
            if (d < 1e-3 || d > 13 || to.normalize().dot(look) < 0.85) continue;
            e.hurt(damageSources().mobAttack(this), 7.0f);
            e.setRemainingFireTicks(100);
        }
        if (enraged) {
            Vec3 c = position();
            for (int r = 2; r <= 5; r += 1) Fx.ring(sl, ParticleTypes.SOUL_FIRE_FLAME, c.add(0, 0.3, 0), r, 10 + r * 4);
            for (LivingEntity e : victims(sl, 5)) {
                e.hurt(damageSources().mobAttack(this), 6.0f);
                knock(e, c, 1.0, 0.4);
            }
        }
    }

    // ------------------------------------------------------------------ helpers

    /** Players and anything else that is not part of the Alliance, within radius. */
    private java.util.List<LivingEntity> victims(ServerLevel sl, double radius) {
        return sl.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(radius),
                x -> x != this && x.isAlive() && !(x instanceof VillainEntity) && x.distanceToSqr(this) <= radius * radius);
    }

    private static void knock(LivingEntity e, Vec3 from, double horizontal, double vertical) {
        Vec3 away = e.position().subtract(from);
        if (away.lengthSqr() < 1e-4) away = new Vec3(0, 0, 1);
        away = away.normalize();
        e.push(away.x * horizontal, vertical, away.z * horizontal);
        e.hurtMarked = true;
    }

    // ------------------------------------------------------------------ boss bar plumbing

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        if (bossEvent != null) bossEvent.setName(getDisplayName());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (hasCustomName()) bossEvent.setName(getDisplayName());
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        if (kind.boss) bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        if (kind.boss) bossEvent.removePlayer(player);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }
}
