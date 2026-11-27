package com.plusultra.client;

import com.plusultra.PlusUltra;
import com.plusultra.client.anim.FxSprites;
import com.plusultra.core.ModEntities;
import com.plusultra.core.ModParticles;
import com.plusultra.entity.VillainEntity.Kind;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid = PlusUltra.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    /** The animation engine spawns its own particles; these types only exist to load their sprites. */
    private static final ParticleProvider<SimpleParticleType> SPRITE_ONLY = (type, level, x, y, z, vx, vy, vz) -> null;

    private ClientModEvents() {}

    @SubscribeEvent
    public static void particles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.GLOW.get(), sprites -> { FxSprites.GLOW = sprites; return SPRITE_ONLY; });
        event.registerSpriteSet(ModParticles.SPARK.get(), sprites -> { FxSprites.SPARK = sprites; return SPRITE_ONLY; });
        event.registerSpriteSet(ModParticles.FEATHER.get(), sprites -> { FxSprites.FEATHER = sprites; return SPRITE_ONLY; });
        event.registerSpriteSet(ModParticles.TAPE.get(), sprites -> { FxSprites.TAPE = sprites; return SPRITE_ONLY; });
        event.registerSpriteSet(ModParticles.PUFF.get(), sprites -> { FxSprites.PUFF = sprites; return SPRITE_ONLY; });
    }

    @SubscribeEvent
    public static void keys(RegisterKeyMappingsEvent event) {
        for (var k : ClientKeys.ABILITY) event.register(k);
        event.register(ClientKeys.TALENTS);
        event.register(ClientKeys.SWAP);
        event.register(ClientKeys.FUSION);
    }

    @SubscribeEvent
    public static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        for (Kind k : Kind.values()) {
            event.registerLayerDefinition(VillainModel.layer(k), () -> VillainModel.createLayer(k));
        }
    }

    @SubscribeEvent
    public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.VILLAIN.get(), c -> new VillainRenderer(c, Kind.DECAY_LORD));
        event.registerEntityRenderer(ModEntities.NOMU.get(), c -> new VillainRenderer(c, Kind.NOMU));
        event.registerEntityRenderer(ModEntities.HERO_KILLER.get(), c -> new VillainRenderer(c, Kind.HERO_KILLER));
        event.registerEntityRenderer(ModEntities.BLUE_FLAME.get(), c -> new VillainRenderer(c, Kind.BLUE_FLAME));
        event.registerEntityRenderer(ModEntities.THUG.get(), c -> new VillainRenderer(c, Kind.THUG));
    }

    @SubscribeEvent
    public static void gui(RegisterGuiLayersEvent event) {
        event.registerAboveAll(PlusUltra.id("quirk_hud"), QuirkHud::render);
    }
}
