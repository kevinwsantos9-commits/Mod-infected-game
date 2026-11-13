package com.nivek.eclipse.client.hud;

import com.nivek.eclipse.client.ClientCombatState;
import com.nivek.eclipse.combat.AbilityPayload;
import com.nivek.eclipse.data.EclipseData;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/** HUD: barra de energia e recarga das habilidades (canto inferior direito). */
public final class EclipseHud {
    private static final int BAR_W = 100;

    private EclipseHud() {}

    public static void render(GuiGraphics g, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;
        if (EclipseData.getMutationLevel(mc.player) < 1) return;

        int x = g.guiWidth() - BAR_W - 10;
        int y = g.guiHeight() - 52;

        int energy = EclipseData.getEnergy(mc.player);
        g.fill(x - 1, y - 1, x + BAR_W + 1, y + 7, 0xFF000000);
        g.fill(x, y, x + BAR_W, y + 6, 0xFF1A2429);
        g.fill(x, y, x + BAR_W * energy / EclipseData.MAX_ENERGY, y + 6, 0xFF9B4DFF);

        drawAbility(g, mc, x, y + 10, "V  Lamina", AbilityPayload.BLADE);
        drawAbility(g, mc, x, y + 20, "R  Garras", AbilityPayload.CLAWS);
    }

    private static void drawAbility(GuiGraphics g, Minecraft mc, int x, int y, String label, int ability) {
        float ready = ClientCombatState.readyFraction(ability);
        g.drawString(mc.font, label, x, y, ready >= 1.0F ? 0xFFE6F2F2 : 0xFF7F9A9A);
        int bx = x + 56;
        g.fill(bx, y + 2, bx + 44, y + 6, 0xFF1A2429);
        g.fill(bx, y + 2, bx + (int) (44 * ready), y + 6, ready >= 1.0F ? 0xFF00E5D0 : 0xFF3A6F6B);
    }
}
