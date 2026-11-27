package com.plusultra.client.anim;

import com.plusultra.net.AnimPayload;
import com.plusultra.quirk.FxType;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;

/** Turns a received {@link AnimPayload} into the matching running animation. */
final class AnimFactory {
    private AnimFactory() {}

    static Anim create(FxType type, AnimPayload p, ClientLevel l) {
        Vec3 a = new Vec3(p.ax(), p.ay(), p.az());
        Vec3 b = new Vec3(p.bx(), p.by(), p.bz());
        int rgb = p.rgb();
        float f = p.f();
        int n = p.n();
        return switch (type) {
            case AURA -> new AnimsCore.Aura(l, p.src(), rgb, n, p.m());
            case LIGHTNING_BURST -> new AnimsCore.LightningBurst(l, a, f, rgb, n > 0 ? n : 6);
            case SMASH -> new AnimsCore.Smash(l, p.src(), a, b, rgb, f > 0 ? f : 1f);
            case LEAP -> new AnimsCore.Leap(l, p.src(), rgb);
            case FLAME_JET -> new AnimsFire.FlameJet(l, p.src(), a, b, f, rgb, p.m());
            case ICE_WAVE -> new AnimsFire.IceWave(l, a, b, f, rgb);
            case DUAL_NOVA -> new AnimsFire.DualNova(l, a, f);
            case MHA_EXPLOSION -> new AnimsFire.MhaExplosion(l, a, f, rgb);
            case PALM_CRACKLE -> new AnimsFire.PalmCrackle(l, p.src(), n > 0 ? n : 3);
            case BLAST_JET -> new AnimsFire.BlastJet(l, p.src(), b, n > 0 ? n : 6);
            case HOWITZER_WINDUP -> new AnimsFire.HowitzerWindup(l, p.src(), a, f, rgb, n > 0 ? n : 8);
            case SHARDS -> new AnimsCore.Shards(l, a, f > 0 ? f : 1f, rgb);
            case GRAVITY_FIELD -> new AnimsFire.GravityField(l, a, f, rgb, p.targets());
            case METEORS -> new AnimsFire.Meteors(l, a, f, rgb);
            case SPEED_TRAIL -> new AnimsCore.SpeedTrail(l, p.src(), rgb, n > 0 ? n : 20, p.m());
            case RING_BURST -> new AnimsCore.RingBurst(l, a, f, rgb, n > 0 ? n : 8);
            case GATHER -> new AnimsCore.Gather(l, a, f, rgb, n > 0 ? n : 8);
            case PROJECTILE -> new AnimsCore.Projectile(l, a, b, rgb, p.m(), n);
            case SHIMMER -> new AnimsCore.Shimmer(l, p.src(), rgb, n > 0 ? n : 30);
            case DOME -> new AnimsCore.Dome(l, a, f, rgb, n > 0 ? n : 40);
            case WINGS -> new AnimsMisc.Wings(l, p.src(), rgb, n > 0 ? n : 12, p.m());
            case FEATHER_VOLLEY -> new AnimsMisc.FeatherVolley(l, p.src(), p.targets(), a, b, n > 0 ? n : 12, rgb);
            case SHADOW_ARM -> new AnimsMisc.ShadowArm(l, p.src(), a, rgb, n > 0 ? n : 7);
            case SHADOW_ERUPT -> new AnimsMisc.ShadowErupt(l, a, f, rgb);
            case STRAND -> new AnimsMisc.Strand(l, p.src(), p.targets(), a, rgb, n > 0 ? n : 10, p.m());
            case ACID_POOL -> new AnimsMisc.AcidPool(l, a, f, rgb, n > 0 ? n : 100, p.m());
            case SOUND_WAVE -> new AnimsMisc.SoundWave(l, p.src(), a, b, f, rgb, n > 0 ? n : 8);
            case RIPPLE -> new AnimsMisc.Ripple(l, a, f, rgb, n > 0 ? n : 2, p.m());
        };
    }
}
