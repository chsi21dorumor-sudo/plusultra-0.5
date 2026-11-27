package com.plusultra.core;

import com.plusultra.PlusUltra;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, PlusUltra.MODID);

    public static final Supplier<CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.plusultra"))
                    .icon(() -> ModItems.QUIRK_ORB.get().getDefaultInstance())
                    .displayItems((params, out) -> {
                        out.accept(ModItems.QUIRK_ORB.get());
                        out.accept(ModItems.QUIRK_ERASER.get());
                        out.accept(ModItems.FUSION_CATALYST.get());
                        out.accept(ModItems.HERO_LICENSE.get());
                        out.accept(ModItems.MISSION_BOARD.get());
                        out.accept(ModItems.GRAPPLING_HOOK.get());
                        out.accept(ModItems.SUPPORT_GLOVES.get());
                        out.accept(ModItems.STAMINA_POTION.get());
                        out.accept(ModItems.GREATER_STAMINA_POTION.get());
                        out.accept(ModItems.HERO_HELMET.get());
                        out.accept(ModItems.HERO_CHESTPLATE.get());
                        out.accept(ModItems.HERO_LEGGINGS.get());
                        out.accept(ModItems.HERO_BOOTS.get());
                        out.accept(ModItems.VILLAIN_SPAWN_EGG.get());
                        out.accept(ModItems.NOMU_SPAWN_EGG.get());
                        out.accept(ModItems.HERO_KILLER_SPAWN_EGG.get());
                        out.accept(ModItems.BLUE_FLAME_SPAWN_EGG.get());
                        out.accept(ModItems.THUG_SPAWN_EGG.get());
                    })
                    .build());

    private ModTabs() {}
}
