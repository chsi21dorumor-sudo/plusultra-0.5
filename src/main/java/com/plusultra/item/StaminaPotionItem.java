package com.plusultra.item;

import com.plusultra.quirk.AbilityManager;
import com.plusultra.quirk.Fx;
import com.plusultra.quirk.PlayerData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/** Drink to restore stamina. amount < 0 restores everything. */
public class StaminaPotionItem extends Item {
    private final float amount;

    public StaminaPotionItem(Properties properties, float amount) {
        super(properties);
        this.amount = amount;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide && player instanceof ServerPlayer sp && !PlayerData.get(sp).hasQuirk()) {
            sp.displayClientMessage(Component.literal("You have no Quirk to recharge.")
                    .withStyle(ChatFormatting.GRAY), true);
            return InteractionResultHolder.fail(player.getItemInHand(hand));
        }
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof ServerPlayer sp) {
            PlayerData d = PlayerData.get(sp);
            float max = d.maxStamina();
            d.stamina = Math.min(max, amount < 0 ? max : d.stamina + amount);
            for (int i = 0; i < d.cd.length; i++) {
                if (amount < 0) d.cd[i] = 0;
            }
            Fx.levelUp(sp);
            AbilityManager.send(sp);
        }
        if (entity instanceof Player player && !player.getAbilities().instabuild) {
            stack.shrink(1);
            ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
            if (stack.isEmpty()) return bottle;
            if (!player.getInventory().add(bottle)) player.drop(bottle, false);
        }
        return stack;
    }
}
