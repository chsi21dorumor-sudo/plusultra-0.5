package com.plusultra.quirk;

import com.plusultra.config.PUConfig;
import com.plusultra.core.ModDimensions;
import com.plusultra.net.SyncPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

/** Validates and runs an ability request coming from a player's keybind. */
public final class AbilityManager {
    private AbilityManager() {}

    /** Slots 0-2 are the active Quirk's abilities, 3 is Fusion Strike, 4 swaps the active Quirk. */
    public static final int SLOT_FUSION_STRIKE = 3;
    public static final int SLOT_SWAP = 4;

    private static void msg(ServerPlayer p, String text, ChatFormatting color) {
        p.displayClientMessage(Component.literal(text).withStyle(color), true);
    }

    public static void use(ServerPlayer p, int slot) {
        if (slot < 0 || slot > 4) return;
        PlayerData d = PlayerData.get(p);
        if (!d.hasQuirk()) {
            msg(p, "You have no Quirk. Use a Quirk Orb to awaken one.", ChatFormatting.GRAY);
            return;
        }
        if (slot == SLOT_SWAP) {
            swap(p, d);
            return;
        }
        if (slot == SLOT_FUSION_STRIKE) {
            fusionStrike(p, d);
            return;
        }

        Quirk q = d.activeQuirk();
        QuirkAbility ability = q.abilities()[slot];

        if (slot == 2 && d.level < PUConfig.ULT_LEVEL.get()) {
            msg(p, "Plus Ultra unlocks at level " + PUConfig.ULT_LEVEL.get() + ".", ChatFormatting.RED);
            return;
        }

        int idx = d.cdBase() + slot;
        long now = p.level().getGameTime();
        if (d.cd[idx] > now) {
            msg(p, String.format("On cooldown: %.1fs", (d.cd[idx] - now) / 20.0), ChatFormatting.GRAY);
            return;
        }

        boolean free = isFree(p);
        double cost = ability.cost() * d.costMultiplier();
        if (!free && d.stamina < cost) {
            msg(p, "Not enough stamina!", ChatFormatting.RED);
            return;
        }

        if (!free) d.stamina -= (float) cost;
        double cooldown = ability.cooldownTicks() * d.cooldownMultiplier() * (free ? 0.5 : 1.0);
        d.cd[idx] = now + Math.max(1L, Math.round(cooldown));

        ability.action().run(p, power(d));

        addXp(p, d, slot == 2 ? 15 : 5);
        send(p);
    }

    private static boolean isFree(ServerPlayer p) {
        return p.isCreative() || p.level().dimension().equals(ModDimensions.TRAINING);
    }

    private static double power(PlayerData d) {
        return 1.0 + 0.12 * (d.level - 1);
    }

    private static void swap(ServerPlayer p, PlayerData d) {
        if (!d.hasFusion()) {
            msg(p, "You have no fused Quirk. Use a Fusion Catalyst at level " + PUConfig.FUSION_LEVEL.get() + ".",
                    ChatFormatting.GRAY);
            return;
        }
        d.useFusion = !d.useFusion;
        Quirk q = d.activeQuirk();
        p.displayClientMessage(Component.literal("Active Quirk: ").withStyle(ChatFormatting.GRAY)
                .append(Component.translatable("quirk.plusultra." + q.id())
                        .withStyle(s -> s.withColor(q.color()))), true);
        send(p);
    }

    /** Fires the first ability of both Quirks at once with a power bonus. */
    private static void fusionStrike(ServerPlayer p, PlayerData d) {
        if (!d.hasFusion()) {
            msg(p, "Fusion Strike needs a fused Quirk.", ChatFormatting.GRAY);
            return;
        }
        if (d.level < PUConfig.ULT_LEVEL.get()) {
            msg(p, "Fusion Strike unlocks at level " + PUConfig.ULT_LEVEL.get() + ".", ChatFormatting.RED);
            return;
        }
        long now = p.level().getGameTime();
        if (d.cd[PlayerData.FUSION_STRIKE] > now) {
            msg(p, String.format("On cooldown: %.1fs", (d.cd[PlayerData.FUSION_STRIKE] - now) / 20.0),
                    ChatFormatting.GRAY);
            return;
        }
        boolean free = isFree(p);
        double cost = 50 * d.costMultiplier();
        if (!free && d.stamina < cost) {
            msg(p, "Not enough stamina!", ChatFormatting.RED);
            return;
        }
        if (!free) d.stamina -= (float) cost;
        double cooldown = 400 * d.cooldownMultiplier() * (free ? 0.5 : 1.0);
        d.cd[PlayerData.FUSION_STRIKE] = now + Math.max(1L, Math.round(cooldown));

        Quirk a = Quirk.byId(d.quirk);
        Quirk b = Quirk.byId(d.fusion);
        double power = power(d) * 1.3;
        Fx.lightning(p.serverLevel(), p.position());
        a.abilities()[0].action().run(p, power);
        b.abilities()[0].action().run(p, power);

        addXp(p, d, 20);
        send(p);
    }

    public static void addXp(ServerPlayer p, PlayerData d, int amount) {
        if (!d.hasQuirk()) return;
        d.xp += amount;
        int max = PUConfig.MAX_LEVEL.get();
        while (d.level < max && d.xp >= d.xpNeeded()) {
            d.xp -= d.xpNeeded();
            d.level++;
            d.skillPoints++;
            d.stamina = d.maxStamina();
            Fx.levelUp(p);
            p.sendSystemMessage(Component.literal("Your Quirk grew stronger! Level " + d.level
                    + "  (+1 skill point, press V)").withStyle(ChatFormatting.GOLD));
            if (d.level == PUConfig.ULT_LEVEL.get()) {
                p.sendSystemMessage(Component.literal("PLUS ULTRA! Your third ability is unlocked.")
                        .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
            }
            if (d.level == PUConfig.FUSION_LEVEL.get()) {
                p.sendSystemMessage(Component.literal("Your Quirk is ready for fusion. Craft a Fusion Catalyst.")
                        .withStyle(ChatFormatting.LIGHT_PURPLE));
            }
        }
        if (d.level >= max) d.xp = 0;
    }

    public static void send(ServerPlayer p) {
        PacketDistributor.sendToPlayer(p, SyncPayload.from(p, PlayerData.get(p)));
    }
}
