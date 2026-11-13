package com.nivek.eclipse.client;

import net.minecraft.client.Minecraft;

/** Recarga das habilidades como o HUD enxerga (informada pelo servidor). */
public final class ClientCombatState {
    private static final long[] END = new long[3];
    private static final int[] TOTAL = new int[3];

    private ClientCombatState() {}

    public static void setCooldown(int ability, int ticks) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || ability < 0 || ability >= END.length) return;
        END[ability] = mc.level.getGameTime() + ticks;
        TOTAL[ability] = Math.max(1, ticks);
    }

    /** Fracao pronta: 0 = acabou de entrar em recarga, 1 = pronta. */
    public static float readyFraction(int ability) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || ability < 0 || ability >= END.length) return 1.0F;
        long remaining = END[ability] - mc.level.getGameTime();
        if (remaining <= 0) return 1.0F;
        return 1.0F - Math.min(1.0F, remaining / (float) TOTAL[ability]);
    }
}
