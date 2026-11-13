package com.nivek.eclipse.event;

import com.nivek.eclipse.data.ModAttachments;
import com.nivek.eclipse.registry.ModBlocks;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/** Evento do meteoro: gatilho, queda simulada no servidor, cratera e nucleo. */
public final class MeteorEvent {
    private static final int FLIGHT_TICKS = 100;
    private static final int CHECK_INTERVAL = 200;   // 10 s
    private static final int CHANCE_ONE_IN = 30;     // media de ~5 min a noite, ao ar livre
    private static final int CRATER_RADIUS = 7;
    private static final int CRATER_DEPTH = 4;

    private static final List<Meteor> ACTIVE = new ArrayList<>();
    private static int checkTimer = 0;

    private static final class Meteor {
        final ServerLevel level;
        final UUID ownerId;
        final BlockPos target;
        final Vec3 vel;
        Vec3 pos;
        int ticksLeft = FLIGHT_TICKS;

        Meteor(ServerLevel level, UUID ownerId, BlockPos target, Vec3 start) {
            this.level = level;
            this.ownerId = ownerId;
            this.target = target;
            this.pos = start;
            Vec3 end = Vec3.atCenterOf(target);
            this.vel = end.subtract(start).scale(1.0 / FLIGHT_TICKS);
        }
    }

    private MeteorEvent() {}

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(MeteorEvent::onServerTick);
    }

    private static void onServerTick(MinecraftServer server) {
        tickMeteors();
        if (++checkTimer >= CHECK_INTERVAL) {
            checkTimer = 0;
            checkTriggers(server);
        }
    }

    private static void checkTriggers(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.isSpectator() || player.getAttachedOrElse(ModAttachments.METEOR_EVENT_DONE, false)) continue;
            if (hasActive(player)) continue;
            if (!(player.level() instanceof ServerLevel level)) continue;
            if (level.dimension() != Level.OVERWORLD || !level.isNight()) continue;
            if (!level.canSeeSky(player.blockPosition())) continue;
            if (level.getRandom().nextInt(CHANCE_ONE_IN) == 0) {
                start(player, level);
            }
        }
    }

    private static boolean hasActive(ServerPlayer player) {
        for (Meteor m : ACTIVE) {
            if (m.ownerId.equals(player.getUUID())) return true;
        }
        return false;
    }

    /** Inicia o meteoro para o jogador e marca o evento como ocorrido. */
    public static void start(ServerPlayer player, ServerLevel level) {
        RandomSource r = level.getRandom();
        double angle = r.nextDouble() * Math.PI * 2;
        double dist = 30 + r.nextInt(16);
        int tx = (int) Math.floor(player.getX() + Math.cos(angle) * dist);
        int tz = (int) Math.floor(player.getZ() + Math.sin(angle) * dist);
        int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, tx, tz) - 1;
        BlockPos target = new BlockPos(tx, surface, tz);
        Vec3 start = new Vec3(tx + Math.cos(angle) * 50, surface + 90, tz + Math.sin(angle) * 50);

        ACTIVE.add(new Meteor(level, player.getUUID(), target, start));
        player.setAttached(ModAttachments.METEOR_EVENT_DONE, true);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.AMBIENT, 2.0F, 0.5F);
        player.displayClientMessage(Component.literal("Uma luz cruza o ceu..."), true);
    }

    private static void tickMeteors() {
        Iterator<Meteor> it = ACTIVE.iterator();
        while (it.hasNext()) {
            Meteor m = it.next();
            m.pos = m.pos.add(m.vel);
            m.ticksLeft--;

            m.level.sendParticles(ParticleTypes.FLAME, m.pos.x, m.pos.y, m.pos.z, 25, 0.8, 0.8, 0.8, 0.05);
            m.level.sendParticles(ParticleTypes.LARGE_SMOKE, m.pos.x, m.pos.y, m.pos.z, 15, 1.2, 1.2, 1.2, 0.02);
            if (m.ticksLeft % 10 == 0) {
                m.level.playSound(null, m.pos.x, m.pos.y, m.pos.z,
                        SoundEvents.FIRECHARGE_USE, SoundSource.AMBIENT, 3.0F, 0.5F);
            }

            if (m.ticksLeft <= 0) {
                impact(m);
                it.remove();
            }
        }
    }

    private static void impact(Meteor m) {
        ServerLevel level = m.level;
        BlockPos c = m.target;
        buildCrater(level, c);
        level.setBlock(new BlockPos(c.getX(), c.getY() - CRATER_DEPTH + 1, c.getZ()),
                ModBlocks.ALIEN_CORE.defaultBlockState(), 3);

        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, c.getX() + 0.5, c.getY() + 1, c.getZ() + 0.5, 3, 3, 1, 3, 0);
        level.playSound(null, c, SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.AMBIENT, 6.0F, 0.6F);
        level.playSound(null, c, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.AMBIENT, 6.0F, 0.4F);

        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(m.ownerId);
        if (owner != null) {
            owner.displayClientMessage(Component.literal("O meteoro atingiu o solo. Algo pulsa na cratera..."), false);
        }
    }

    private static void buildCrater(ServerLevel level, BlockPos c) {
        int r = CRATER_RADIUS;
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > r) continue;
                int depth = (int) Math.round(CRATER_DEPTH * (1.0 - (dist * dist) / (r * r)));
                int floorY = c.getY() - depth;

                for (int y = floorY + 1; y <= c.getY() + 10; y++) {
                    BlockPos pos = new BlockPos(c.getX() + dx, y, c.getZ() + dz);
                    BlockState state = level.getBlockState(pos);
                    if (state.isAir() || state.getDestroySpeed(level, pos) < 0) continue;
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                }

                if (depth >= 1) {
                    BlockPos floor = new BlockPos(c.getX() + dx, floorY, c.getZ() + dz);
                    BlockState old = level.getBlockState(floor);
                    if (old.getDestroySpeed(level, floor) >= 0) {
                        BlockState floorState = dist < r * 0.5 ? Blocks.BLACKSTONE.defaultBlockState()
                                : Blocks.COARSE_DIRT.defaultBlockState();
                        level.setBlock(floor, floorState, 3);
                    }
                }
            }
        }
    }
}
