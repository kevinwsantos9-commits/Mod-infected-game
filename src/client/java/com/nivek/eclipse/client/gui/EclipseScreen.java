package com.nivek.eclipse.client.gui;

import com.nivek.eclipse.data.EclipseData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/** Interface principal do ECLIPSE SYSTEM: navegacao lateral + conteudo da aba. */
public class EclipseScreen extends Screen {
    private static final int SIDEBAR_W = 104;
    private static final int HEADER_H = 22;
    private static final int MAX_LEVEL = 5;
    private static final String[] STAGES = {
            "Humano", "Fusao inicial", "Mutacao ativa", "Hibrido", "Avancado", "Eclipse"
    };

    private static final int COLOR_BG = 0xFF0B0F14;
    private static final int COLOR_SIDEBAR = 0xFF080B0F;
    private static final int COLOR_ACCENT = 0xFF00E5D0;
    private static final int COLOR_SELECTED = 0xFF12323A;
    private static final int COLOR_HOVER = 0xFF0F1D23;
    private static final int COLOR_TEXT = 0xFFE6F2F2;
    private static final int COLOR_DIM = 0xFF7F9A9A;

    private EclipseTab selected = EclipseTab.OVERVIEW;
    private int px, py, pw, ph, rowH;

    public EclipseScreen() {
        super(Component.literal("ECLIPSE SYSTEM"));
    }

    @Override
    protected void init() {
        pw = Math.min(this.width - 20, 420);
        ph = Math.min(this.height - 20, 240);
        px = (this.width - pw) / 2;
        py = (this.height - ph) / 2;
        rowH = Math.max(10, Math.min(16, (ph - HEADER_H - 12) / EclipseTab.values().length));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private int tabTop(int index) {
        return py + HEADER_H + 6 + index * rowH;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, this.width, this.height, 0xB0000000);

        // painel + borda
        g.fill(px - 1, py - 1, px + pw + 1, py + ph + 1, COLOR_ACCENT);
        g.fill(px, py, px + pw, py + ph, COLOR_BG);

        // cabecalho
        g.drawString(this.font, "ECLIPSE SYSTEM", px + 8, py + 7, COLOR_ACCENT);
        g.fill(px, py + HEADER_H - 1, px + pw, py + HEADER_H, COLOR_ACCENT);

        // barra lateral
        g.fill(px, py + HEADER_H, px + SIDEBAR_W, py + ph, COLOR_SIDEBAR);
        EclipseTab[] tabs = EclipseTab.values();
        for (int i = 0; i < tabs.length; i++) {
            int top = tabTop(i);
            boolean hover = mouseX >= px && mouseX < px + SIDEBAR_W && mouseY >= top && mouseY < top + rowH;
            if (tabs[i] == selected) {
                g.fill(px, top, px + SIDEBAR_W, top + rowH, COLOR_SELECTED);
                g.fill(px, top, px + 2, top + rowH, COLOR_ACCENT);
            } else if (hover) {
                g.fill(px, top, px + SIDEBAR_W, top + rowH, COLOR_HOVER);
            }
            g.drawString(this.font, tabs[i].label, px + 8, top + (rowH - 8) / 2,
                    tabs[i] == selected ? COLOR_TEXT : COLOR_DIM);
        }

        // conteudo
        int cx = px + SIDEBAR_W + 12;
        int cy = py + HEADER_H + 10;
        g.drawString(this.font, selected.label, cx, cy, COLOR_ACCENT);
        if (selected == EclipseTab.OVERVIEW) {
            renderOverview(g, cx, cy + 16);
        } else {
            g.drawString(this.font, "Disponivel em breve (" + selected.note + ").", cx, cy + 16, COLOR_DIM);
        }
    }

    private void renderOverview(GuiGraphics g, int x, int y) {
        Player player = this.minecraft != null ? this.minecraft.player : null;
        if (player == null) return;

        int level = Math.min(EclipseData.getMutationLevel(player), MAX_LEVEL);
        g.drawString(this.font, "Jogador: " + player.getName().getString(), x, y, COLOR_TEXT);
        g.drawString(this.font, "Estagio: " + STAGES[level], x, y + 14, COLOR_TEXT);
        g.drawString(this.font, "Mutacao: " + level + " / " + MAX_LEVEL, x, y + 28, COLOR_TEXT);

        int barW = Math.max(40, pw - SIDEBAR_W - 36);
        int barY = y + 44;
        g.fill(x, barY, x + barW, barY + 8, 0xFF1A2429);
        g.fill(x, barY, x + barW * level / MAX_LEVEL, barY + 8, COLOR_ACCENT);

        int energy = EclipseData.getEnergy(player);
        g.drawString(this.font, "Energia: " + energy + " / " + EclipseData.MAX_ENERGY, x, y + 58, COLOR_TEXT);
        int energyY = y + 72;
        g.fill(x, energyY, x + barW, energyY + 8, 0xFF1A2429);
        g.fill(x, energyY, x + barW * energy / EclipseData.MAX_ENERGY, energyY + 8, 0xFF9B4DFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && mouseX >= px && mouseX < px + SIDEBAR_W) {
            EclipseTab[] tabs = EclipseTab.values();
            for (int i = 0; i < tabs.length; i++) {
                int top = tabTop(i);
                if (mouseY >= top && mouseY < top + rowH) {
                    selected = tabs[i];
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
