package com.nivek.eclipse.client.render;

import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Detecta o momento em que um jogador ganha a mutacao 1 e calcula o progresso da animacao. */
public final class ArmTransformTracker {
    private static final float DURATION_TICKS = 40.0F;
    private static final Map<UUID, Integer> LAST_LEVEL = new HashMap<>();
    private static final Map<UUID, Float> START_TIME = new HashMap<>();

    private ArmTransformTracker() {}

    public static float update(Player player, int level, float partialTick) {
        UUID id = player.getUUID();
        float now = player.level().getGameTime() + partialTick;
        int last = LAST_LEVEL.getOrDefault(id, -1);
        if (last == 0 && level >= 1) {
            START_TIME.put(id, now);
        }
        LAST_LEVEL.put(id, level);
        Float start = START_TIME.get(id);
        if (start == null) return 1.0F;
        float p = (now - start) / DURATION_TICKS;
        if (p >= 1.0F) {
            START_TIME.remove(id);
            return 1.0F;
        }
        return Math.max(0.0F, p);
    }
}
