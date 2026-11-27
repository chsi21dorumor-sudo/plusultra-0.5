package com.plusultra.client.anim;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/** One running client-side animation. {@link #step()} is called once per client tick. */
public abstract class Anim {
    protected final ClientLevel level;
    /** Ticks since the animation started (0 on the first step). */
    protected int t = 0;

    protected Anim(ClientLevel level) {
        this.level = level;
    }

    /** @return true when the animation is finished and should be removed. */
    public final boolean tick() {
        boolean done = step();
        t++;
        return done;
    }

    protected abstract boolean step();

    protected Entity ent(int id) {
        return id < 0 ? null : level.getEntity(id);
    }

    /** Approximate position of the entity's right hand. */
    protected static Vec3 hand(Entity e) {
        Vec3 f = P.flat(e.getLookAngle());
        Vec3 r = new Vec3(-f.z, 0, f.x);
        return e.getEyePosition().add(f.scale(0.35)).add(r.scale(0.35)).add(0, -0.5, 0);
    }

    protected static Vec3 rightOf(Entity e) {
        Vec3 f = P.flat(e.getLookAngle());
        return new Vec3(-f.z, 0, f.x);
    }

    /** Y of the first solid-looking block surface below the point (searches up to 8 blocks down). */
    protected double groundY(Vec3 p) {
        for (int i = 0; i <= 8; i++) {
            BlockPos pos = BlockPos.containing(p.x, p.y - i, p.z);
            if (!level.getBlockState(pos).isAir()) return pos.getY() + 1.0;
        }
        return p.y;
    }

    protected Vec3 body(Entity e) {
        return new Vec3(e.getX() + P.rs() * 0.35, e.getY() + P.R.nextDouble() * e.getBbHeight(),
                e.getZ() + P.rs() * 0.35);
    }
}
