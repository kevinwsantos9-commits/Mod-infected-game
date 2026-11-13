package com.nivek.eclipse.combat;

import com.nivek.eclipse.data.EclipseData;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Combate: energia, cooldowns, combos, Lamina Biologica e Garras.
 * Toda a validacao e no servidor; o cliente so pede a habilidade.
 */
public final class CombatManager {
    public record AbilityDef(int id, int cost, int cooldown, float damage, double range, double minDot) {}

    public static final AbilityDef BLADE = new AbilityDef(AbilityPayload.BLADE, 20, 30, 8.0F, 3.5, 0.4);
    public static final AbilityDef CLAWS = new AbilityDef(AbilityPayload.CLAWS, 8, 10, 4.0F, 2.8, 0.5);

    private static final int COMBO_WINDOW_TICKS = 40;
    private static final int COMBO_FINISHER = 3;
    private static final int REGEN_INTERVAL = 10;
    private static final int REGEN_AMOUNT = 2;

    /** Fim do cooldown (game time) por habilidade: indice = id da habilidade. */
    private static final Map<UUID, long[]> READY = new HashMap<>();
    /** {contagem do combo, game time do ultimo golpe}. */
    private static final Map<UUID, long[]> COMBO = new HashMap<>();
    private static int regenTimer = 0;

    private CombatManager() {}

    public static void register() {
        PayloadTypeRegistry.playC2S().register(AbilityPayload.TYPE, AbilityPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(CooldownPayload.TYPE, CooldownPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(AbilityPayload.TYPE,
                (payload, context) -> handle(context.player(), payload.ability()));
        ServerTickEvents.END_SERVER_TICK.register(CombatManager::tick);
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            UUID id = handler.getPlayer().getUUID();
            READY.remove(id);
            COMBO.remove(id);
        });
    }

    private static void handle(ServerPlayer player, int ability) {
        if (!(player.level() instanceof ServerLevel level)) return;
        if (!player.isAlive() || player.isSpectator()) return;
        if (EclipseData.getMutationLevel(player) < 1) return;
        if (ability == AbilityPayload.BLADE) {
            useAbility(player, level, BLADE);
        } else if (ability == AbilityPayload.CLAWS) {
            useAbility(player, level, CLAWS);
        }
    }

    private static void tick(MinecraftServer server) {
        if (++regenTimer < REGEN_INTERVAL) return;
        regenTimer = 0;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (EclipseData.getMutationLevel(player) < 1) continue;
            int energy = EclipseData.getEnergy(player);
            if (energy < EclipseData.MAX_ENERGY) {
                EclipseData.setEnergy(player, energy + REGEN_AMOUNT);
            }
        }
    }

    private static void useAbility(ServerPlayer player, ServerLevel level, AbilityDef def) {
        UUID uuid = player.getUUID();
        long now = level.getGameTime();
        long[] ready = READY.computeIfAbsent(uuid, k -> new long[3]);
        if (now < ready[def.id()]) {
            player.displayClientMessage(Component.literal("Em recarga."), true);
            return;
        }
        int energy = EclipseData.getEnergy(player);
        if (energy < def.cost()) {
            player.displayClientMessage(Component.literal("Energia insuficiente."), true);
            return;
        }
        EclipseData.setEnergy(player, energy - def.cost());
        ready[def.id()] = now + def.cooldown();
        ServerPlayNetworking.send(player, new CooldownPayload(def.id(), def.cooldown()));

        // combo: golpes encadeados dentro da janela aumentam o dano; o 3o e o golpe final
        long[] combo = COMBO.computeIfAbsent(uuid, k -> new long[2]);
        int count = (combo[0] > 0 && now - combo[1] <= COMBO_WINDOW_TICKS) ? (int) combo[0] + 1 : 1;
        boolean finisher = count >= COMBO_FINISHER;
        combo[0] = finisher ? 0 : count;
        combo[1] = now;
        float multiplier = 1.0F + 0.25F * (Math.min(count, COMBO_FINISHER) - 1);
        if (finisher) {
            player.displayClientMessage(Component.literal("COMBO FINAL!"), true);
        } else if (count >= 2) {
            player.displayClientMessage(Component.literal("Combo x" + count), true);
        }

        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        AABB box = player.getBoundingBox().inflate(def.range());
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box,
                e -> e != player && e.isAlive() && !e.isSpectator())) {
            Vec3 to = target.position().add(0.0, target.getBbHeight() / 2.0, 0.0).subtract(eye);
            double dist = to.length();
            if (dist > def.range() || dist < 1.0E-4) continue;
            Vec3 dir = to.normalize();
            if (dir.dot(look) < def.minDot()) continue;
            target.hurtServer(level, level.damageSources().playerAttack(player), def.damage() * multiplier);
            if (finisher) {
                target.push(dir.x * 0.8, 0.35, dir.z * 0.8);
                target.hurtMarked = true;
            }
        }

        if (def.id() == AbilityPayload.BLADE) {
            bladeEffects(level, player, eye, look);
        } else {
            clawEffects(level, player, eye, look);
        }
        if (finisher) {
            Vec3 front = eye.add(look.scale(2.0));
            level.sendParticles(ParticleTypes.EXPLOSION, front.x, front.y - 0.3, front.z, 1, 0, 0, 0, 0);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_ATTACK_KNOCKBACK, SoundSource.PLAYERS, 1.2F, 0.8F);
        }
    }

    private static void bladeEffects(ServerLevel level, ServerPlayer player, Vec3 eye, Vec3 look) {
        Vec3 front = eye.add(look.scale(1.8));
        level.sendParticles(ParticleTypes.SWEEP_ATTACK, front.x, front.y - 0.3, front.z, 1, 0, 0, 0, 0);
        for (int i = -5; i <= 5; i++) {
            double a = i * 0.18;
            double dx = look.x * Math.cos(a) - look.z * Math.sin(a);
            double dz = look.x * Math.sin(a) + look.z * Math.cos(a);
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    eye.x + dx * 2.2, eye.y - 0.4, eye.z + dz * 2.2, 2, 0.05, 0.05, 0.05, 0.01);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 0.7F);
    }

    private static void clawEffects(ServerLevel level, ServerPlayer player, Vec3 eye, Vec3 look) {
        Vec3 right = look.cross(new Vec3(0.0, 1.0, 0.0));
        boolean hasSide = right.length() > 1.0E-4;
        if (hasSide) right = right.normalize();
        // tres riscos paralelos a frente do jogador
        for (int k = -1; k <= 1; k++) {
            for (int t = 0; t <= 4; t++) {
                Vec3 p = eye.add(look.scale(1.0 + 0.4 * t));
                if (hasSide) p = p.add(right.scale(k * 0.35));
                level.sendParticles(ParticleTypes.CRIT, p.x, p.y - 0.25 - 0.05 * t, p.z, 1, 0, 0, 0, 0);
            }
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0F, 1.4F);
    }
}
