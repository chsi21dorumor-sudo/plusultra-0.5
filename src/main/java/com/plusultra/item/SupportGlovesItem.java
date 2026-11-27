package com.plusultra.item;

import com.plusultra.quirk.Abilities;
import com.plusultra.quirk.Fx;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Support gadget: right-click for an impact punch that hurts and launches enemies in front of you. */
public class SupportGlovesItem extends Item {
    public SupportGlovesItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(stack);
        if (!level.isClientSide && player instanceof ServerPlayer sp && level instanceof ServerLevel sl) {
            List<LivingEntity> hits = Abilities.cone(sp, 4.5, 0.6);
            Vec3 look = sp.getLookAngle();
            Vec3 pt = sp.getEyePosition().add(look.scale(2.2));
            for (LivingEntity t : hits) {
                Abilities.hurt(sp, t, 6);
                Abilities.shove(t, Abilities.flat(look), 1.5, 0.45);
            }
            Fx.burst(sl, Fx.dust(0xFFC107, 1.4f), pt, 20, 0.5, 0.08);
            Fx.ring(sl, ParticleTypes.CRIT, pt, 1.2, 14);
            sl.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.PLAYER_ATTACK_STRONG,
                    net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 0.9f);
            sp.getCooldowns().addCooldown(this, 50);
            if (!hits.isEmpty()) stack.hurtAndBreak(1, sp, LivingEntity.getSlotForHand(hand));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
