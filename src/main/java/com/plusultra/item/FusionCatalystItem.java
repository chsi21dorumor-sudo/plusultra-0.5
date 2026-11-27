package com.plusultra.item;

import com.plusultra.config.PUConfig;
import com.plusultra.quirk.AbilityManager;
import com.plusultra.quirk.Fx;
import com.plusultra.quirk.PlayerData;
import com.plusultra.quirk.Quirk;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Fuses a second random Quirk into a player whose Quirk has reached the fusion level. */
public class FusionCatalystItem extends Item {
    public FusionCatalystItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            PlayerData d = PlayerData.get(sp);
            if (!d.hasQuirk()) {
                warn(sp, "You need a Quirk before you can fuse another.");
                return InteractionResultHolder.fail(stack);
            }
            if (d.hasFusion()) {
                warn(sp, "You already carry a fused Quirk. Use a Quirk Eraser to remove it.");
                return InteractionResultHolder.fail(stack);
            }
            int need = PUConfig.FUSION_LEVEL.get();
            if (d.level < need) {
                warn(sp, "Your Quirk must be level " + need + " to survive fusion.");
                return InteractionResultHolder.fail(stack);
            }

            Quirk q;
            do {
                q = Quirk.random(sp.getRandom());
            } while (q.id().equals(d.quirk));
            d.fusion = q.id();
            d.useFusion = false;
            stack.consume(1, sp);

            final Quirk fused = q;
            sp.sendSystemMessage(Component.literal("Quirk fusion! You gained: ")
                    .append(Component.translatable("quirk.plusultra." + fused.id())
                            .withStyle(s -> s.withColor(fused.color())))
                    .withStyle(ChatFormatting.BOLD));
            sp.sendSystemMessage(Component.literal("Press G to swap Quirks, B for Fusion Strike (needs Plus Ultra level).")
                    .withStyle(ChatFormatting.GRAY));

            if (level instanceof ServerLevel sl) {
                Vec3 c = sp.position();
                Quirk primary = Quirk.byId(d.quirk);
                Fx.spiral(sl, Fx.dust(primary.color(), 1.4f), c, 2.4, 0.9, 3, 40);
                Fx.spiral(sl, Fx.dust(fused.color(), 1.4f), c, 2.4, 0.9, -3, 40);
                Fx.sphere(sl, ParticleTypes.END_ROD, c.add(0, 1, 0), 1.5, 40);
                Fx.lightning(sl, c);
                sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1f, 1f);
            }
            AbilityManager.send(sp);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private static void warn(ServerPlayer sp, String text) {
        sp.displayClientMessage(Component.literal(text).withStyle(ChatFormatting.RED), true);
    }
}
