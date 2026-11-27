package com.plusultra.client.anim;

import com.mojang.logging.LogUtils;
import com.plusultra.net.AnimPayload;
import com.plusultra.quirk.FxType;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

/** Runs every active animation once per client tick, and owns the camera shake and screen flash state. */
public final class ClientAnims {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final List<Anim> ACTIVE = new ArrayList<>();
    private static final List<Anim> PENDING = new ArrayList<>();

    private static int shakeTicks = 0;
    private static int shakeMax = 1;
    private static float shakeAmp = 0f;

    private static int flashTicks = 0;
    private static int flashMax = 1;
    private static int flashRgb = 0xFFFFFF;
    private static float flashAlpha = 0f;

    private ClientAnims() {}

    // ------------------------------------------------------------------ lifecycle

    public static void start(AnimPayload p) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        FxType type = FxType.byId(p.kind());
        if (type == null) return;
        try {
            Anim a = AnimFactory.create(type, p, level);
            if (a != null) PENDING.add(a);
        } catch (RuntimeException e) {
            LOGGER.error("Plus Ultra: failed to start animation {}", type, e);
        }
    }

    /** For animations that start other animations. Safe to call from inside {@link Anim#step()}. */
    public static void add(Anim a) {
        PENDING.add(a);
    }

    public static void tick() {
        if (Minecraft.getInstance().level == null) {
            ACTIVE.clear();
            PENDING.clear();
            shakeTicks = 0;
            flashTicks = 0;
            return;
        }
        if (!PENDING.isEmpty()) {
            ACTIVE.addAll(PENDING);
            PENDING.clear();
        }
        ACTIVE.removeIf(a -> {
            try {
                return a.tick();
            } catch (RuntimeException e) {
                LOGGER.error("Plus Ultra: animation crashed, removing it", e);
                return true;
            }
        });
        if (shakeTicks > 0) shakeTicks--;
        if (flashTicks > 0) flashTicks--;
    }

    // ------------------------------------------------------------------ camera shake

    public static void shake(int ticks, float amp) {
        if (amp <= currentShake()) return;
        shakeTicks = ticks;
        shakeMax = Math.max(1, ticks);
        shakeAmp = amp;
    }

    /** Shake scaled by how close the local player is to the event. */
    public static void shakeNear(Vec3 center, double radius, int ticks, float amp) {
        var player = Minecraft.getInstance().player;
        if (player == null) return;
        double d = player.position().distanceTo(center);
        if (d >= radius) return;
        shake(ticks, (float) (amp * (1.0 - d / radius)));
    }

    /** Current shake strength, 0 when idle. */
    public static float currentShake() {
        return shakeTicks <= 0 ? 0f : shakeAmp * shakeTicks / (float) shakeMax;
    }

    // ------------------------------------------------------------------ screen flash

    public static void flashNear(Vec3 center, double radius, int ticks, int rgb, float alpha) {
        var player = Minecraft.getInstance().player;
        if (player == null) return;
        double d = player.position().distanceTo(center);
        if (d >= radius) return;
        float a = (float) (alpha * (1.0 - d / radius));
        if (a <= 0.02f || (flashTicks > 0 && a < flashAlpha)) return;
        flashTicks = ticks;
        flashMax = Math.max(1, ticks);
        flashRgb = rgb;
        flashAlpha = a;
    }

    /** Draws the flash over the whole screen. Called from the HUD layer. */
    public static void renderFlash(GuiGraphics g) {
        if (flashTicks <= 0) return;
        int a = (int) (255 * flashAlpha * flashTicks / flashMax);
        if (a <= 2) return;
        g.fill(0, 0, g.guiWidth(), g.guiHeight(), (a << 24) | (flashRgb & 0xFFFFFF));
    }
}
