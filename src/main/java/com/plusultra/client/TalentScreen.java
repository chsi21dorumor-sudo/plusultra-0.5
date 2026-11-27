package com.plusultra.client;

import com.plusultra.net.TalentPayload;
import com.plusultra.quirk.Quirk;
import com.plusultra.quirk.Talent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

/** Talent tree screen (default key V): spend skill points, or respec for free. */
public class TalentScreen extends Screen {
    private static final int W = 280;
    private static final int ROW = 32;

    private final Button[] plus = new Button[Talent.values().length];
    private int left;
    private int top;
    private int panelHeight;

    public TalentScreen() {
        super(Component.translatable("screen.plusultra.talents"));
    }

    @Override
    protected void init() {
        Talent[] ts = Talent.values();
        panelHeight = 74 + ts.length * ROW;
        left = (width - W) / 2;
        top = (height - panelHeight) / 2;

        for (int i = 0; i < ts.length; i++) {
            final int idx = i;
            plus[i] = addRenderableWidget(Button.builder(Component.literal("+"),
                            b -> PacketDistributor.sendToServer(new TalentPayload(idx)))
                    .bounds(left + W - 32, top + 46 + i * ROW, 22, 20).build());
        }
        addRenderableWidget(Button.builder(Component.translatable("screen.plusultra.respec"),
                        b -> PacketDistributor.sendToServer(new TalentPayload(TalentPayload.RESPEC)))
                .bounds(left + 10, top + panelHeight - 28, 90, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose())
                .bounds(left + W - 100, top + panelHeight - 28, 90, 20).build());
        refresh();
    }

    private void refresh() {
        Talent[] ts = Talent.values();
        for (int i = 0; i < ts.length && i < plus.length; i++) {
            if (plus[i] != null) {
                plus[i].active = ClientData.skillPoints > 0 && ClientData.talents[i] < ts[i].maxRank;
            }
        }
    }

    @Override
    public void tick() {
        refresh();
        if (!ClientData.hasQuirk()) onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /** The panel is drawn in render(); skipping the default blurred background avoids double-blur errors. */
    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (ClientKeys.TALENTS.matches(keyCode, scanCode)) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        Quirk q = Quirk.byId(ClientData.quirk);
        int rgb = q == null ? 0xFFFFFF : q.color();
        int color = 0xFF000000 | rgb;

        g.fill(0, 0, width, height, 0x99000000);
        g.fillGradient(left, top, left + W, top + panelHeight, 0xF0181626, 0xF00A0912);
        g.renderOutline(left, top, W, panelHeight, color);
        g.fill(left, top, left + W, top + 3, color);

        Component title = Component.translatable("screen.plusultra.talents");
        g.drawString(font, title, left + 10, top + 10, color, true);
        if (q != null) {
            g.drawString(font, Component.translatable("quirk.plusultra." + q.id())
                    .append(Component.literal("  Lv." + ClientData.level)), left + 10, top + 22, 0xFFCCCCCC, true);
        }
        String sp = ClientData.skillPoints + " skill point" + (ClientData.skillPoints == 1 ? "" : "s");
        g.drawString(font, sp, left + W - 10 - font.width(sp), top + 10,
                ClientData.skillPoints > 0 ? 0xFF55FF55 : 0xFF888888, true);

        Talent[] ts = Talent.values();
        for (int i = 0; i < ts.length; i++) {
            int ry = top + 44 + i * ROW;
            g.fill(left + 8, ry - 2, left + W - 8, ry + ROW - 6, 0x55000000);
            g.drawString(font, Component.translatable("talent.plusultra." + ts[i].id), left + 14, ry, 0xFFFFFFFF, true);
            g.drawString(font, Component.translatable("talent.plusultra." + ts[i].id + ".desc"),
                    left + 14, ry + 11, 0xFF9A9AB0, true);
            int rank = ClientData.talents[i];
            for (int r = 0; r < ts[i].maxRank; r++) {
                int px = left + W - 120 + r * 12;
                g.fill(px, ry + 2, px + 9, ry + 11, 0xFF000000);
                g.fill(px + 1, ry + 3, px + 8, ry + 10, r < rank ? color : 0xFF2A2A38);
            }
        }
        super.render(g, mouseX, mouseY, partialTick);
    }
}
