package com.nivek.eclipse.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.nivek.eclipse.data.EclipseData;
import com.nivek.eclipse.data.ModAttachments;
import com.nivek.eclipse.event.MeteorEvent;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/** Comandos de teste (op nivel 2): /eclipse meteor | status | mutation <0-5> | reset. */
public final class EclipseCommand {
    private EclipseCommand() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(Commands.literal("eclipse")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("meteor").executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            MeteorEvent.start(player, (ServerLevel) player.level());
                            ctx.getSource().sendSuccess(() -> Component.literal("Meteoro iniciado."), false);
                            return 1;
                        }))
                        .then(Commands.literal("status").executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            int mutation = EclipseData.getMutationLevel(player);
                            boolean meteor = player.getAttachedOrElse(ModAttachments.METEOR_EVENT_DONE, false);
                            ctx.getSource().sendSuccess(() -> Component.literal(
                                    "Mutacao: " + mutation + " | Meteoro ja ocorreu: " + meteor), false);
                            return 1;
                        }))
                        .then(Commands.literal("mutation")
                                .then(Commands.argument("level", IntegerArgumentType.integer(0, 5)).executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    int level = IntegerArgumentType.getInteger(ctx, "level");
                                    EclipseData.setMutationLevel(player, level);
                                    ctx.getSource().sendSuccess(() -> Component.literal("Mutacao definida: " + level), false);
                                    return 1;
                                })))
                        .then(Commands.literal("reset").executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            player.setAttached(ModAttachments.METEOR_EVENT_DONE, false);
                            EclipseData.setMutationLevel(player, 0);
                            ctx.getSource().sendSuccess(() -> Component.literal("Meteoro e mutacao resetados."), false);
                            return 1;
                        }))));
    }
}
