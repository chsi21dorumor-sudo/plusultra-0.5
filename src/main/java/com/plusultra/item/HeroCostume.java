package com.plusultra.item;

import com.plusultra.core.ModItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;

/** Set bonus helper: +5% stamina regen per piece, +10% Quirk damage with all four. */
public final class HeroCostume {
    private HeroCostume() {}

    public static int pieces(Player p) {
        int n = 0;
        if (p.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.HERO_HELMET)) n++;
        if (p.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.HERO_CHESTPLATE)) n++;
        if (p.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.HERO_LEGGINGS)) n++;
        if (p.getItemBySlot(EquipmentSlot.FEET).is(ModItems.HERO_BOOTS)) n++;
        return n;
    }

    public static double regenBonus(Player p) {
        return 1.0 + 0.05 * pieces(p);
    }
}
