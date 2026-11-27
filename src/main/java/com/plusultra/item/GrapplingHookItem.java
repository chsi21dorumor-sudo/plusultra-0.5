package com.plusultra.item;

import com.plusultra.quirk.Abilities;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Support gadget: right-click to zip toward the block you are looking at. Works without a Quirk. */
public class GrapplingHookItem extends Item {
    public GrapplingHookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(stack);
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            if (!Abilities.grapple(sp, 32, 2.3)) {
                sp.displayClientMessage(Component.literal("Nothing in range to hook onto.")
                        .withStyle(ChatFormatting.GRAY), true);
                return InteractionResultHolder.fail(stack);
            }
            sp.getCooldowns().addCooldown(this, 30);
            stack.hurtAndBreak(1, sp, LivingEntity.getSlotForHand(hand));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
