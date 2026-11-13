package com.nivek.eclipse.data;

import net.minecraft.world.entity.player.Player;

/** Acesso simples aos dados do jogador. */
public final class EclipseData {
    public static final int MAX_ENERGY = 100;

    private EclipseData() {}

    public static int getEnergy(Player player) {
        return player.getAttachedOrElse(ModAttachments.ENERGY, MAX_ENERGY);
    }

    public static void setEnergy(Player player, int energy) {
        player.setAttached(ModAttachments.ENERGY, Math.max(0, Math.min(MAX_ENERGY, energy)));
    }

    public static int getMutationLevel(Player player) {
        return player.getAttachedOrElse(ModAttachments.MUTATION_LEVEL, 0);
    }

    public static void setMutationLevel(Player player, int level) {
        player.setAttached(ModAttachments.MUTATION_LEVEL, Math.max(0, level));
    }
}
