package com.plusultra.core;

import com.plusultra.PlusUltra;
import com.plusultra.item.FusionCatalystItem;
import com.plusultra.item.GrapplingHookItem;
import com.plusultra.item.StaminaPotionItem;
import com.plusultra.item.SupportGlovesItem;
import com.plusultra.item.HeroLicenseItem;
import com.plusultra.item.QuirkEraserItem;
import com.plusultra.item.QuirkOrbItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(PlusUltra.MODID);

    public static final DeferredItem<QuirkOrbItem> QUIRK_ORB = ITEMS.register("quirk_orb",
            () -> new QuirkOrbItem(new Item.Properties().stacksTo(16).rarity(Rarity.RARE)));

    public static final DeferredItem<QuirkEraserItem> QUIRK_ERASER = ITEMS.register("quirk_eraser",
            () -> new QuirkEraserItem(new Item.Properties().stacksTo(8).rarity(Rarity.UNCOMMON)));

    public static final DeferredItem<FusionCatalystItem> FUSION_CATALYST = ITEMS.register("fusion_catalyst",
            () -> new FusionCatalystItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    public static final DeferredItem<HeroLicenseItem> HERO_LICENSE = ITEMS.register("hero_license",
            () -> new HeroLicenseItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<BlockItem> MISSION_BOARD =
            ITEMS.registerSimpleBlockItem("mission_board", ModBlocks.MISSION_BOARD);

    public static final DeferredItem<DeferredSpawnEggItem> VILLAIN_SPAWN_EGG = ITEMS.register("villain_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.VILLAIN, 0x2B1B3D, 0xB22222, new Item.Properties()));

    // ---- Support items
    public static final DeferredItem<GrapplingHookItem> GRAPPLING_HOOK = ITEMS.register("grappling_hook",
            () -> new GrapplingHookItem(new Item.Properties().durability(250)));

    public static final DeferredItem<SupportGlovesItem> SUPPORT_GLOVES = ITEMS.register("support_gloves",
            () -> new SupportGlovesItem(new Item.Properties().durability(200)));

    public static final DeferredItem<StaminaPotionItem> STAMINA_POTION = ITEMS.register("stamina_potion",
            () -> new StaminaPotionItem(new Item.Properties().stacksTo(16), 60f));

    public static final DeferredItem<StaminaPotionItem> GREATER_STAMINA_POTION = ITEMS.register(
            "greater_stamina_potion",
            () -> new StaminaPotionItem(new Item.Properties().stacksTo(8).rarity(Rarity.RARE), -1f));

    // ---- Hero Costume (set bonus handled in HeroCostume)
    public static final DeferredItem<ArmorItem> HERO_HELMET = ITEMS.register("hero_helmet",
            () -> new ArmorItem(ModArmor.HERO, ArmorItem.Type.HELMET,
                    new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(20))));

    public static final DeferredItem<ArmorItem> HERO_CHESTPLATE = ITEMS.register("hero_chestplate",
            () -> new ArmorItem(ModArmor.HERO, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(20))));

    public static final DeferredItem<ArmorItem> HERO_LEGGINGS = ITEMS.register("hero_leggings",
            () -> new ArmorItem(ModArmor.HERO, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(20))));

    public static final DeferredItem<ArmorItem> HERO_BOOTS = ITEMS.register("hero_boots",
            () -> new ArmorItem(ModArmor.HERO, ArmorItem.Type.BOOTS,
                    new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(20))));

    // ---- Villain spawn eggs (the original villain egg is above)
    public static final DeferredItem<DeferredSpawnEggItem> NOMU_SPAWN_EGG = ITEMS.register("nomu_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.NOMU, 0x3A3A44, 0xF0A0B0, new Item.Properties()));

    public static final DeferredItem<DeferredSpawnEggItem> HERO_KILLER_SPAWN_EGG = ITEMS.register(
            "hero_killer_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.HERO_KILLER, 0x151018, 0xC0222A, new Item.Properties()));

    public static final DeferredItem<DeferredSpawnEggItem> BLUE_FLAME_SPAWN_EGG = ITEMS.register(
            "blue_flame_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.BLUE_FLAME, 0x1B1B26, 0x38C8FF, new Item.Properties()));

    public static final DeferredItem<DeferredSpawnEggItem> THUG_SPAWN_EGG = ITEMS.register("thug_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.THUG, 0x555560, 0x8B5A2B, new Item.Properties()));

    private ModItems() {}
}
