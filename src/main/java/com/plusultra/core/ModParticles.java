package com.plusultra.core;

import com.plusultra.PlusUltra;
import java.util.function.Supplier;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Particle types whose textures feed the client animation engine. They are never spawned through the
 * normal particle path; the client animations create the particles directly and only borrow the sprites.
 */
public final class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, PlusUltra.MODID);

    public static final Supplier<SimpleParticleType> GLOW = PARTICLES.register("glow", () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> SPARK = PARTICLES.register("spark", () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> FEATHER = PARTICLES.register("feather", () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> TAPE = PARTICLES.register("tape", () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> PUFF = PARTICLES.register("puff", () -> new SimpleParticleType(false));

    private ModParticles() {}
}
