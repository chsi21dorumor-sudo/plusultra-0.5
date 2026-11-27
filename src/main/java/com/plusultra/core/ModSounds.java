package com.plusultra.core;

import com.plusultra.PlusUltra;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Custom ability sound effects. The files live in assets/plusultra/sounds and are listed in sounds.json. */
public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, PlusUltra.MODID);

    private static Supplier<SoundEvent> reg(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(PlusUltra.id(name)));
    }

    public static final Supplier<SoundEvent> HEAVYPUNCH = reg("heavypunch");
    public static final Supplier<SoundEvent> DASH = reg("dash");
    public static final Supplier<SoundEvent> LIGHTNING_STRIKE = reg("lightning_strike");
    public static final Supplier<SoundEvent> FIRE = reg("fire");
    public static final Supplier<SoundEvent> ICE = reg("ice");
    public static final Supplier<SoundEvent> ICE2 = reg("ice2");
    public static final Supplier<SoundEvent> HEARDENING = reg("heardening");
    public static final Supplier<SoundEvent> PUNCH = reg("punch");
    public static final Supplier<SoundEvent> KINZOKU1 = reg("kinzoku1");
    public static final Supplier<SoundEvent> EFFECT = reg("effect");
    public static final Supplier<SoundEvent> ROCK = reg("rock");
    public static final Supplier<SoundEvent> HEAVYSWING = reg("heavyswing");
    public static final Supplier<SoundEvent> LASER = reg("laser");
    public static final Supplier<SoundEvent> CHARGE = reg("charge");
    public static final Supplier<SoundEvent> SWING = reg("swing");
    public static final Supplier<SoundEvent> GLASS = reg("glass");
    public static final Supplier<SoundEvent> TORNADO = reg("tornado");
    public static final Supplier<SoundEvent> WING = reg("wing");
    public static final Supplier<SoundEvent> BITE_STRONG = reg("bite_strong");
    public static final Supplier<SoundEvent> DECAY = reg("decay");
    public static final Supplier<SoundEvent> BLOOD_1 = reg("blood_1");
    public static final Supplier<SoundEvent> MUCHI = reg("muchi");
    public static final Supplier<SoundEvent> WHIP = reg("whip");
    public static final Supplier<SoundEvent> WATER = reg("water");
    public static final Supplier<SoundEvent> ONPA = reg("onpa");
    public static final Supplier<SoundEvent> HAJIKU = reg("hajiku");
    public static final Supplier<SoundEvent> MUCHI_SWING = reg("muchi_swing");

    private ModSounds() {}
}
