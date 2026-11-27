package com.plusultra.mission;

import com.plusultra.core.ModEntities;
import com.plusultra.core.ModItems;
import com.plusultra.entity.VillainEntity;
import com.plusultra.quirk.AbilityManager;
import com.plusultra.quirk.Fx;
import com.plusultra.quirk.PlayerData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

/**
 * Villain Alliance raid: three waves of enemies around the player.
 * Wave 1: thugs. Wave 2: thugs + a Hero Killer. Wave 3: a boss with escorts.
 * Fails if the player dies, leaves the area or dimension, or takes too long.
 */
public final class AllianceRaid {
    public static final String MISSION = "alliance";
    public static final int WAVES = 3;

    private static final int WARNING_TICKS = 100;
    private static final int TIMEOUT_TICKS = 20 * 60 * 12;
    private static final double MAX_DISTANCE = 96;

    private static final class Raid {
        final UUID player;
        final ServerLevel level;
        final ResourceKey<Level> dimension;
        final Vec3 center;
        final ServerBossEvent bar = new ServerBossEvent(Component.literal("Villain Alliance Raid"),
                BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_6);
        final List<UUID> alive = new ArrayList<>();
        int wave = 0;          // waves cleared so far
        int total = 1;         // enemies in the current wave
        int delay = WARNING_TICKS;
        boolean waiting = true;
        int ticks = 0;

        Raid(ServerPlayer p) {
            this.player = p.getUUID();
            this.level = p.serverLevel();
            this.dimension = p.level().dimension();
            this.center = p.position();
        }
    }

    private static final Map<UUID, Raid> RAIDS = new HashMap<>();

    private AllianceRaid() {}

    public static boolean isActive(ServerPlayer p) {
        return RAIDS.containsKey(p.getUUID());
    }

    public static boolean start(ServerPlayer p) {
        if (isActive(p)) return false;
        Raid raid = new Raid(p);
        RAIDS.put(p.getUUID(), raid);
        raid.bar.setName(Component.literal("Villain Alliance Raid - incoming..."));
        raid.bar.setProgress(1.0f);
        raid.bar.addPlayer(p);

        PlayerData d = PlayerData.get(p);
        d.mission = MISSION;
        d.missionProgress = 0;
        d.missionGoal = WAVES;
        p.sendSystemMessage(Component.literal("ALLIANCE RAID! The Villain Alliance is closing in. Hold your ground!")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        AbilityManager.send(p);
        return true;
    }

    /** Silently ends a raid (mission abandoned or admin command). */
    public static void cancel(ServerPlayer p) {
        Raid raid = RAIDS.get(p.getUUID());
        if (raid != null) end(raid, null, false, "Raid ended.");
    }

    public static void clear() {
        for (Raid r : new ArrayList<>(RAIDS.values())) r.bar.removeAllPlayers();
        RAIDS.clear();
    }

    public static void tick(MinecraftServer server) {
        if (RAIDS.isEmpty()) return;
        for (Raid raid : new ArrayList<>(RAIDS.values())) {
            ServerPlayer p = server.getPlayerList().getPlayer(raid.player);
            if (p == null) {
                end(raid, null, false, "The raid ended because the hero left.");
                continue;
            }
            if (!p.isAlive()) {
                end(raid, p, false, "You fell to the Villain Alliance. The raid is over.");
                continue;
            }
            if (!p.level().dimension().equals(raid.dimension) || p.position().distanceTo(raid.center) > MAX_DISTANCE) {
                end(raid, p, false, "You abandoned the battlefield. The raid is over.");
                continue;
            }
            if (++raid.ticks > TIMEOUT_TICKS) {
                end(raid, p, false, "The raid dragged on too long and the villains escaped.");
                continue;
            }

            if (raid.waiting) {
                int seconds = Math.max(0, raid.delay / 20);
                raid.bar.setName(Component.literal("Villain Alliance - Wave " + (raid.wave + 1) + "/" + WAVES
                        + " arriving in " + seconds + "s"));
                if (--raid.delay <= 0) {
                    spawnWave(p, raid);
                    raid.waiting = false;
                }
                continue;
            }

            if (raid.ticks % 10 != 0) continue;
            raid.alive.removeIf(id -> {
                Entity e = raid.level.getEntity(id);
                return e == null || !e.isAlive();
            });
            int remaining = raid.alive.size();
            raid.bar.setProgress(raid.total <= 0 ? 0f : Math.min(1f, remaining / (float) raid.total));
            raid.bar.setName(Component.literal("Villain Alliance - Wave " + (raid.wave + 1) + "/" + WAVES
                    + "  (" + remaining + " left)"));

            if (remaining == 0) waveCleared(p, raid);
        }
    }

    private static void waveCleared(ServerPlayer p, Raid raid) {
        raid.wave++;
        PlayerData d = PlayerData.get(p);
        d.missionProgress = raid.wave;
        if (raid.wave >= WAVES) {
            victory(p, raid, d);
            return;
        }
        raid.waiting = true;
        raid.delay = WARNING_TICKS;
        p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1));
        p.sendSystemMessage(Component.literal("Wave " + raid.wave + " cleared! Catch your breath - the next one is coming.")
                .withStyle(ChatFormatting.GREEN));
        AbilityManager.send(p);
    }

    private static void victory(ServerPlayer p, Raid raid, PlayerData d) {
        p.sendSystemMessage(Component.literal("The Villain Alliance has been defeated!")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        give(p, new ItemStack(ModItems.STAMINA_POTION.get(), 2));
        if (p.getRandom().nextFloat() < 0.35f) give(p, new ItemStack(ModItems.GREATER_STAMINA_POTION.get()));
        if (p.getRandom().nextFloat() < 0.15f) give(p, new ItemStack(ModItems.FUSION_CATALYST.get()));
        Missions.complete(p, d);
        end(raid, p, true, null);
    }

    private static void give(ServerPlayer p, ItemStack stack) {
        if (!p.getInventory().add(stack)) p.drop(stack, false);
    }

    private static void end(Raid raid, ServerPlayer p, boolean success, String message) {
        raid.bar.removeAllPlayers();
        RAIDS.remove(raid.player);
        if (!success) {
            for (UUID id : raid.alive) {
                Entity e = raid.level.getEntity(id);
                if (e != null) e.discard();
            }
        }
        if (p != null) {
            PlayerData d = PlayerData.get(p);
            if (!success && MISSION.equals(d.mission)) {
                d.mission = "";
                d.missionProgress = 0;
                d.missionGoal = 0;
            }
            if (message != null) p.sendSystemMessage(Component.literal(message).withStyle(ChatFormatting.GRAY));
            AbilityManager.send(p);
        }
    }

    // ------------------------------------------------------------------ spawning

    private static void spawnWave(ServerPlayer p, Raid raid) {
        List<EntityType<VillainEntity>> types = new ArrayList<>();
        switch (raid.wave) {
            case 0 -> {
                for (int i = 0; i < 5; i++) types.add(ModEntities.THUG.get());
            }
            case 1 -> {
                for (int i = 0; i < 4; i++) types.add(ModEntities.THUG.get());
                types.add(ModEntities.HERO_KILLER.get());
            }
            default -> {
                for (int i = 0; i < 3; i++) types.add(ModEntities.THUG.get());
                types.add(ModEntities.randomFinaleBoss(p.getRandom()));
                types.add(ModEntities.HERO_KILLER.get());
            }
        }

        raid.alive.clear();
        ServerLevel level = raid.level;
        for (EntityType<VillainEntity> type : types) {
            double angle = p.getRandom().nextDouble() * Math.PI * 2;
            double dist = 18 + p.getRandom().nextDouble() * 8;
            double x = p.getX() + Math.cos(angle) * dist;
            double z = p.getZ() + Math.sin(angle) * dist;
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) x, (int) z);

            VillainEntity v = type.create(level);
            if (v == null) continue;
            v.moveTo(x, y, z, p.getRandom().nextFloat() * 360f, 0f);
            v.setPersistenceRequired();
            v.setTarget(p);
            v.addEffect(new MobEffectInstance(MobEffects.GLOWING, Integer.MAX_VALUE, 0, false, false));
            level.addFreshEntity(v);
            raid.alive.add(v.getUUID());

            Fx.ring(level, Fx.dust(0x8B0000, 1.6f), new Vec3(x, y + 0.1, z), 1.4, 18);
            level.sendParticles(ParticleTypes.LARGE_SMOKE, x, y + 1, z, 20, 0.4, 0.8, 0.4, 0.02);
        }
        raid.total = Math.max(1, raid.alive.size());
        raid.bar.setProgress(1.0f);
        level.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 3.0f, 1.0f);
        p.sendSystemMessage(Component.literal("Wave " + (raid.wave + 1) + "/" + WAVES + " has arrived! Defeat them all.")
                .withStyle(ChatFormatting.RED));
    }
}
