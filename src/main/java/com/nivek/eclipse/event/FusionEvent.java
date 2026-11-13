package com.nivek.eclipse.event;

import com.nivek.eclipse.data.EclipseData;
import com.nivek.eclipse.registry.ModBlocks;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Primeira fusao: sequencia de ~5 s ao tocar o nucleo.
 * O nucleo so e consumido e a mutacao nivel 1 so e concedida no FIM da sequencia;
 * se a fusao for cancelada (morte, desconexao), nada e perdido.
 */
public final class FusionEvent {
    private static final int DURATION = 100;
    private static final Map<UUID, Fusion> ACTIVE = new HashMap<>();

    private static final class Fusion {
        final ServerLevel level;
        final BlockPos core;
        final Vec3 anchor;
        int tick = 0;

        Fusion(ServerLevel level, BlockPos core, Vec3 anchor) {
            this.level = level;
            this.core = core;
            this.anchor = anchor;
        }
    }

    private FusionEvent() {}

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(FusionEvent::onServerTick);
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> ACTIVE.remove(handler.getPlayer().getUUID()));
    }

    public static void tryStart(ServerPlayer player, ServerLevel level, BlockPos core) {
        if (ACTIVE.containsKey(player.getUUID())) return;
        if (EclipseData.getMutationLevel(player) >= 1) {
            player.displayClientMessage(Component.literal("Seu corpo ja carrega a marca do Eclipse."), true);
            return;
        }
        for (Fusion f : ACTIVE.values()) {
            if (f.core.equals(core)) {
                player.displayClientMessage(Component.literal("O nucleo esta ocupado."), true);
                return;
            }
        }
        ACTIVE.put(player.getUUID(), new Fusion(level, core, player.position()));
        level.playSound(null, core, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.0F, 0.5F);
        player.displayClientMessage(Component.literal("O nucleo se abre..."), true);
    }

    private static void onServerTick(MinecraftServer server) {
        Iterator<Map.Entry<UUID, Fusion>> it = ACTIVE.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Fusion> entry = it.next();
            Fusion f = entry.getValue();
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());

            if (player == null || !player.isAlive() || player.level() != f.level
                    || !f.level.getBlockState(f.core).is(ModBlocks.ALIEN_CORE)) {
                if (player != null) {
                    player.displayClientMessage(Component.literal("A fusao foi interrompida."), true);
                }
                it.remove();
                continue;
            }

            // segura o jogador no lugar durante a sequencia
            if (player.position().distanceToSqr(f.anchor) > 0.01) {
                player.teleportTo(f.anchor.x, f.anchor.y, f.anchor.z);
            }

            spawnTentacles(f.level, f.anchor, f.tick);
            f.level.sendParticles(ParticleTypes.PORTAL, f.anchor.x, f.anchor.y + 1.0, f.anchor.z, 5, 0.5, 0.8, 0.5, 0.5);

            if (f.tick % 20 == 0) {
                f.level.playSound(null, f.anchor.x, f.anchor.y, f.anchor.z,
                        SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 2.0F, 0.8F + f.tick / 200.0F);
            }
            if (f.tick == 60) {
                f.level.playSound(null, f.anchor.x, f.anchor.y, f.anchor.z,
                        SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.PLAYERS, 1.0F, 0.6F);
                player.displayClientMessage(Component.literal("Algo toma o seu corpo..."), true);
            }

            f.tick++;
            if (f.tick >= DURATION) {
                complete(f, player);
                it.remove();
            }
        }
    }

    private static void spawnTentacles(ServerLevel level, Vec3 base, int tick) {
        double progress = Math.min(1.0, tick / 60.0);
        int samples = 14;
        for (int i = 0; i < 6; i++) {
            double baseAngle = i * (Math.PI * 2 / 6) + tick * 0.06;
            for (int s = 0; s <= samples; s++) {
                double t = s / (double) samples;
                if (t > progress) break;
                double radius = 3.0 * (1.0 - t) + 0.45 * t;
                double angle = baseAngle + t * 1.5;
                double wave = Math.sin(tick * 0.3 + t * 6 + i) * 0.15;
                double x = base.x + Math.cos(angle) * (radius + wave);
                double y = base.y + 0.1 + t * 1.5;
                double z = base.z + Math.sin(angle) * (radius + wave);
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 1, 0, 0, 0, 0);
            }
        }
    }

    private static void complete(Fusion f, ServerPlayer player) {
        EclipseData.setMutationLevel(player, 1);
        f.level.removeBlock(f.core, false);

        f.level.sendParticles(ParticleTypes.END_ROD, f.anchor.x, f.anchor.y + 1.0, f.anchor.z, 120, 0.6, 1.0, 0.6, 0.25);
        f.level.playSound(null, f.anchor.x, f.anchor.y, f.anchor.z,
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 2.0F, 0.6F);
        f.level.playSound(null, f.anchor.x, f.anchor.y, f.anchor.z,
                SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 3.0F, 0.5F);
        player.displayClientMessage(Component.literal("Mutacao nivel 1 desbloqueada."), false);
    }
}
