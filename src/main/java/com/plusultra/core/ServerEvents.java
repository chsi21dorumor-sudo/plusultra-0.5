package com.plusultra.core;

import com.plusultra.PlusUltra;
import com.plusultra.agency.AgencyData;
import com.plusultra.command.ModCommands;
import com.plusultra.config.PUConfig;
import com.plusultra.item.HeroCostume;
import com.plusultra.mission.AllianceRaid;
import com.plusultra.mission.Missions;
import com.plusultra.quirk.Scheduler;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import com.plusultra.quirk.AbilityManager;
import com.plusultra.quirk.PlayerData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = PlusUltra.MODID)
public final class ServerEvents {
    private ServerEvents() {}

    @SubscribeEvent
    public static void onCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        PlayerData d = PlayerData.get(sp);

        // Agency synergy: +20% regen per nearby agency member (max 3), recomputed once a second.
        if (sp.tickCount % 20 == 0) {
            double synergy = 1.0;
            AgencyData.Agency agency = AgencyData.get(sp.server).agencyOf(sp.getUUID());
            if (agency != null) {
                int near = 0;
                for (ServerPlayer other : sp.serverLevel().players()) {
                    if (other != sp && agency.members.contains(other.getUUID())
                            && other.distanceToSqr(sp) <= 16 * 16) near++;
                }
                synergy += 0.2 * Math.min(near, 3);
            }
            d.synergy = synergy;
        }

        if (d.hasQuirk()) {
            float max = d.maxStamina();
            if (d.stamina < max) {
                d.stamina = Math.min(max, d.stamina + (float) (PUConfig.STAMINA_REGEN.get() / 20.0 * d.synergy
                        * d.regenMultiplier() * HeroCostume.regenBonus(sp)));
            }
        }

        if (sp.tickCount % 5 == 0 && (d.hasQuirk() || d.points > 0 || Missions.hasMission(d))) {
            AbilityManager.send(sp);
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) AbilityManager.send(sp);
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            PlayerData d = PlayerData.get(sp);
            d.stamina = d.maxStamina();
            AbilityManager.send(sp);
        }
    }

    @SubscribeEvent
    public static void onKill(LivingDeathEvent event) {
        Entity src = event.getSource().getEntity();
        if (src instanceof ServerPlayer sp) {
            Missions.onKill(sp, event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        Scheduler.tick();
        AllianceRaid.tick(event.getServer());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        Scheduler.clear();
        AllianceRaid.clear();
    }
}
