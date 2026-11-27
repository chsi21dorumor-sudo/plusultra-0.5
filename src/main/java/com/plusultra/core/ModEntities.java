package com.plusultra.core;

import com.plusultra.PlusUltra;
import com.plusultra.entity.VillainEntity;
import com.plusultra.entity.VillainEntity.Kind;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = PlusUltra.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, PlusUltra.MODID);

    /** The original Decay Lord keeps the id "villain" so old worlds and eggs still work. */
    public static final DeferredHolder<EntityType<?>, EntityType<VillainEntity>> VILLAIN = ENTITIES.register(
            "villain",
            () -> EntityType.Builder.<VillainEntity>of((t, l) -> new VillainEntity(t, l, Kind.DECAY_LORD),
                            MobCategory.MONSTER)
                    .sized(0.6f, 1.95f).clientTrackingRange(10).build("villain"));

    public static final DeferredHolder<EntityType<?>, EntityType<VillainEntity>> NOMU = ENTITIES.register(
            "nomu",
            () -> EntityType.Builder.<VillainEntity>of((t, l) -> new VillainEntity(t, l, Kind.NOMU),
                            MobCategory.MONSTER)
                    .sized(0.9f, 2.6f).clientTrackingRange(10).build("nomu"));

    public static final DeferredHolder<EntityType<?>, EntityType<VillainEntity>> HERO_KILLER = ENTITIES.register(
            "hero_killer",
            () -> EntityType.Builder.<VillainEntity>of((t, l) -> new VillainEntity(t, l, Kind.HERO_KILLER),
                            MobCategory.MONSTER)
                    .sized(0.6f, 1.95f).clientTrackingRange(10).build("hero_killer"));

    public static final DeferredHolder<EntityType<?>, EntityType<VillainEntity>> BLUE_FLAME = ENTITIES.register(
            "blue_flame",
            () -> EntityType.Builder.<VillainEntity>of((t, l) -> new VillainEntity(t, l, Kind.BLUE_FLAME),
                            MobCategory.MONSTER)
                    .sized(0.6f, 2.0f).fireImmune().clientTrackingRange(10).build("blue_flame"));

    public static final DeferredHolder<EntityType<?>, EntityType<VillainEntity>> THUG = ENTITIES.register(
            "thug",
            () -> EntityType.Builder.<VillainEntity>of((t, l) -> new VillainEntity(t, l, Kind.THUG),
                            MobCategory.MONSTER)
                    .sized(0.6f, 1.9f).clientTrackingRange(10).build("thug"));

    /** One of the four boss kinds, for villain missions and the final raid wave. */
    public static EntityType<VillainEntity> randomBoss(RandomSource rng) {
        return switch (rng.nextInt(4)) {
            case 0 -> VILLAIN.get();
            case 1 -> NOMU.get();
            case 2 -> HERO_KILLER.get();
            default -> BLUE_FLAME.get();
        };
    }

    /** A boss for the raid finale: never the glass-cannon Hero Killer, who has his own slot in wave 2. */
    public static EntityType<VillainEntity> randomFinaleBoss(RandomSource rng) {
        return switch (rng.nextInt(3)) {
            case 0 -> VILLAIN.get();
            case 1 -> NOMU.get();
            default -> BLUE_FLAME.get();
        };
    }

    @SubscribeEvent
    public static void attributes(EntityAttributeCreationEvent event) {
        event.put(VILLAIN.get(), VillainEntity.createAttributes(Kind.DECAY_LORD).build());
        event.put(NOMU.get(), VillainEntity.createAttributes(Kind.NOMU).build());
        event.put(HERO_KILLER.get(), VillainEntity.createAttributes(Kind.HERO_KILLER).build());
        event.put(BLUE_FLAME.get(), VillainEntity.createAttributes(Kind.BLUE_FLAME).build());
        event.put(THUG.get(), VillainEntity.createAttributes(Kind.THUG).build());
    }

    private ModEntities() {}
}
