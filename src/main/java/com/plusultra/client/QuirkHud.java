package com.plusultra.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.plusultra.PlusUltra;
import com.plusultra.client.anim.ClientAnims;
import com.plusultra.config.PUConfig;
import com.plusultra.mission.Ranks;
import com.plusultra.quirk.Quirk;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

/**
 * Top-left HUD panel: Quirk name and level, stamina bar with numbers, XP bar,
 * ability rows that fill as they recharge, and fusion / rank / mission lines.
 */
public final class QuirkHud {
    private static final int W = 128;
    private static final int ROW = 16;
    private static final int ICON = 13;
    private static final Map<String, ResourceLocation> ICONS = new HashMap<>();

    private QuirkHud() {}

    private static ResourceLocation icon(String path) {
        return ICONS.computeIfAbsent(path, PlusUltra::id);
    }

    private static int shade(int rgb, float f) {
        int r = Math.min(255, (int) (((rgb >> 16) & 255) * f));
        int gr = Math.min(255, (int) (((rgb >> 8) & 255) * f));
        int b = Math.min(255, (int) ((rgb & 255) * f));
        return 0xFF000000 | (r << 16) | (gr << 8) | b;
    }

    public static void render(GuiGraphics g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.player == null) return;
        ClientAnims.renderFlash(g);
        Quirk q = Quirk.byId(ClientData.quirk);
        if (q == null) return;

        int rgb = q.color();
        int color = 0xFF000000 | rgb;
        boolean hasOther = !ClientData.other.isEmpty();
        boolean hasMission = !ClientData.mission.isEmpty();
        boolean hasSp = ClientData.skillPoints > 0;

        int x = 10, y = 10;
        int h = 6 + 12 + 14 + 8 + 3 * ROW + (hasOther ? ROW + 11 : 0) + 12 + (hasMission ? 10 : 0)
                + (hasSp ? 11 : 0) + 4;

        // panel
        g.fillGradient(x - 5, y - 5, x + W + 5, y - 5 + h, 0xB0141420, 0xB0050508);
        g.renderOutline(x - 5, y - 5, W + 10, h, (0xCC << 24) | rgb);
        g.fill(x - 5, y - 5, x - 3, y - 5 + h, color); // accent stripe

        // header
        g.drawString(mc.font, Component.translatable("quirk.plusultra." + q.id()), x, y, color, true);
        String lv = "Lv." + ClientData.level;
        g.drawString(mc.font, lv, x + W - mc.font.width(lv), y, 0xFFFFFFFF, true);
        y += 12;

        // stamina bar with numbers
        float frac = ClientData.maxStamina <= 0 ? 0 : Math.min(1f, ClientData.stamina / ClientData.maxStamina);
        g.fill(x - 1, y - 1, x + W + 1, y + 11, 0xFF000000);
        g.fill(x, y, x + W, y + 10, 0xFF1B1B26);
        int fillW = (int) (W * frac);
        if (fillW > 0) g.fillGradient(x, y, x + fillW, y + 10, shade(rgb, 1.25f), shade(rgb, 0.65f));
        String st = (int) ClientData.stamina + " / " + (int) ClientData.maxStamina;
        g.drawString(mc.font, st, x + (W - mc.font.width(st)) / 2, y + 1, 0xFFFFFFFF, true);
        y += 14;

        // xp bar
        float xpFrac = ClientData.xpNeeded <= 0 ? 0 : Math.min(1f, (float) ClientData.xp / ClientData.xpNeeded);
        g.fill(x - 1, y - 1, x + W + 1, y + 4, 0xFF000000);
        g.fill(x, y, x + (int) (W * xpFrac), y + 3, 0xFFFFD700);
        y += 8;

        // ability rows: the bar fills as the ability recharges
        for (int i = 0; i < 3; i++) {
            boolean locked = i == 2 && ClientData.level < PUConfig.ULT_LEVEL.get();
            int cd = ClientData.cooldownTicks(i);
            abilityRow(g, mc, x, y, rgb, ClientKeys.ABILITY[i].getTranslatedKeyMessage(),
                    Component.translatable("ability.plusultra." + q.id() + "." + q.abilities()[i].id()),
                    cd, q.abilities()[i].cooldownTicks(), locked,
                    locked ? "Lv." + PUConfig.ULT_LEVEL.get() : null,
                    icon("textures/gui/ability/" + q.id() + "_" + q.abilities()[i].id() + ".png"));
            y += ROW;
        }

        if (hasOther) {
            boolean locked = ClientData.level < PUConfig.ULT_LEVEL.get();
            abilityRow(g, mc, x, y, 0xC77DFF, ClientKeys.FUSION.getTranslatedKeyMessage(),
                    Component.translatable("hud.plusultra.fusion_strike"),
                    ClientData.cooldownTicks(3), 400, locked, locked ? "Lv." + PUConfig.ULT_LEVEL.get() : null,
                    icon("textures/gui/ability/fusion.png"));
            y += ROW;
            Quirk o = Quirk.byId(ClientData.other);
            MutableComponent swap = Component.literal("[").append(ClientKeys.SWAP.getTranslatedKeyMessage())
                    .append(Component.literal("] Swap to "));
            if (o != null) swap.append(Component.translatable("quirk.plusultra." + o.id()));
            RenderSystem.enableBlend();
            g.blit(icon("textures/gui/ability/swap.png"), x, y - 1, 10, 10, 0f, 0f, 32, 32, 32, 32);
            g.drawString(mc.font, swap, x + 13, y, 0xFF9A9AB0, true);
            y += 11;
        }

        y += 2;
        g.drawString(mc.font, Ranks.of(ClientData.points).name() + " - " + ClientData.points + " pts",
                x, y, 0xFFFFD700, true);
        y += 10;
        if (hasMission) {
            String txt = switch (ClientData.mission) {
                case "patrol" -> "Patrol " + ClientData.missionProgress + "/" + ClientData.missionGoal;
                case "villain" -> "Capture the villain";
                case "alliance" -> "Alliance raid: wave " + Math.min(ClientData.missionProgress + 1, ClientData.missionGoal)
                        + "/" + ClientData.missionGoal;
                default -> "";
            };
            g.drawString(mc.font, txt, x, y, 0xFFFFFF55, true);
            y += 10;
        }
        if (hasSp) {
            MutableComponent sp = Component.literal("[").append(ClientKeys.TALENTS.getTranslatedKeyMessage())
                    .append(Component.literal("] " + ClientData.skillPoints + " skill point"
                            + (ClientData.skillPoints == 1 ? "" : "s")));
            g.drawString(mc.font, sp, x, y, 0xFF55FF55, true);
        }
    }

    private static void abilityRow(GuiGraphics g, Minecraft mc, int x, int y, int rgb, Component key,
                                   Component name, int cd, int cdMax, boolean locked, String tag,
                                   ResourceLocation icon) {
        g.fill(x, y, x + W, y + ROW - 1, 0xFF14141C);
        if (!locked) {
            float ready = cd <= 0 ? 1f : 1f - Math.min(1f, cd / (float) Math.max(1, cdMax));
            int alpha = cd <= 0 ? 0x88 : 0x55;
            int w = (int) (W * ready);
            if (w > 0) g.fill(x, y, x + w, y + ROW - 1, (alpha << 24) | rgb);
        }

        // icon, with a dark sweep that shrinks as the ability recharges
        int ix = x + 1, iy = y + 1;
        RenderSystem.enableBlend();
        g.blit(icon, ix, iy, ICON, ICON, 0f, 0f, 32, 32, 32, 32);
        if (locked) {
            g.fill(ix, iy, ix + ICON, iy + ICON, 0xC0000000);
        } else if (cd > 0) {
            int cover = (int) (ICON * Math.min(1f, cd / (float) Math.max(1, cdMax)));
            if (cover > 0) g.fill(ix, iy, ix + ICON, iy + cover, 0xB0000000);
        }

        int textColor = locked ? 0xFF777777 : (cd > 0 ? 0xFFBBBBBB : 0xFFFFFFFF);
        MutableComponent line = Component.literal("[").append(key).append(Component.literal("] ")).append(name);
        g.drawString(mc.font, line, x + ICON + 4, y + 4, textColor, true);
        String right = locked ? tag : (cd > 0 ? String.format("%.1fs", cd / 20.0) : null);
        if (right != null) g.drawString(mc.font, right, x + W - mc.font.width(right) - 2, y + 4, textColor, true);
    }
}
