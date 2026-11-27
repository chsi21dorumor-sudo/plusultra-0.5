package com.plusultra.client.anim;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;

/**
 * A fully controllable billboard particle: exact velocity, gravity, friction, lifetime, tint, size ramp,
 * spin and fade. Always rendered at full brightness.
 */
public class FxParticle extends TextureSheetParticle {
    private final float startSize;
    private final float endSize;
    private final float startAlpha;
    private final float spin;

    public FxParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz,
                      SpriteSet sprites, int rgb, float size, float endSize, int life, float gravity,
                      float friction, float spin, float alpha) {
        super(level, x, y, z);
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
        this.lifetime = Math.max(1, life);
        this.gravity = gravity;
        this.friction = friction;
        this.hasPhysics = false;
        this.quadSize = size;
        this.startSize = size;
        this.endSize = endSize;
        this.startAlpha = alpha;
        this.spin = spin;
        this.setColor(((rgb >> 16) & 255) / 255f, ((rgb >> 8) & 255) / 255f, (rgb & 255) / 255f);
        this.alpha = alpha;
        this.pickSprite(sprites);
        this.roll = this.random.nextFloat() * (float) (Math.PI * 2);
        this.oRoll = this.roll;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.isAlive()) return;
        float f = (float) this.age / (float) this.lifetime;
        this.oRoll = this.roll;
        this.roll += this.spin;
        this.quadSize = this.startSize + (this.endSize - this.startSize) * f;
        this.alpha = this.startAlpha * (f < 0.6f ? 1.0f : Math.max(0.0f, (1.0f - f) / 0.4f));
    }

    @Override
    public int getLightColor(float partialTick) {
        return LightTexture.FULL_BRIGHT;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }
}
