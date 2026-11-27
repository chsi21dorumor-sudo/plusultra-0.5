package com.plusultra.core;

import com.plusultra.PlusUltra;
import java.util.EnumMap;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Armor material for the Hero Costume. Textures: textures/models/armor/hero_layer_1.png and hero_layer_2.png. */
public final class ModArmor {
    public static final DeferredRegister<ArmorMaterial> MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, PlusUltra.MODID);

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> HERO = MATERIALS.register("hero",
            () -> new ArmorMaterial(
                    Util.make(new EnumMap<>(ArmorItem.Type.class), m -> {
                        m.put(ArmorItem.Type.BOOTS, 2);
                        m.put(ArmorItem.Type.LEGGINGS, 5);
                        m.put(ArmorItem.Type.CHESTPLATE, 6);
                        m.put(ArmorItem.Type.HELMET, 2);
                        m.put(ArmorItem.Type.BODY, 5);
                    }),
                    12,
                    SoundEvents.ARMOR_EQUIP_LEATHER,
                    () -> Ingredient.of(Items.LEATHER),
                    List.of(new ArmorMaterial.Layer(PlusUltra.id("hero"))),
                    0.5f,
                    0.0f));

    private ModArmor() {}
}
