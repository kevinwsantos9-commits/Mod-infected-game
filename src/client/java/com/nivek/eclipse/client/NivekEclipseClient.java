package com.nivek.eclipse.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.nivek.eclipse.NivekEclipse;
import com.nivek.eclipse.client.gui.EclipseScreen;
import com.nivek.eclipse.client.hud.EclipseHud;
import com.nivek.eclipse.client.render.BioArmLayer;
import com.nivek.eclipse.client.render.BioArmModel;
import com.nivek.eclipse.combat.AbilityPayload;
import com.nivek.eclipse.combat.CooldownPayload;
import com.nivek.eclipse.data.EclipseData;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.network.chat.Component;

public class NivekEclipseClient implements ClientModInitializer {
    private static KeyMapping openSystemKey;
    private static KeyMapping bladeKey;
    private static KeyMapping clawsKey;

    @Override
    public void onInitializeClient() {
        openSystemKey = key("key.nivekeclipse.open_system", InputConstants.KEY_G);
        bladeKey = key("key.nivekeclipse.blade", InputConstants.KEY_V);
        clawsKey = key("key.nivekeclipse.claws", InputConstants.KEY_R);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (bladeKey.consumeClick()) requestAbility(client, AbilityPayload.BLADE);
            while (clawsKey.consumeClick()) requestAbility(client, AbilityPayload.CLAWS);
            while (openSystemKey.consumeClick()) {
                if (client.player == null || client.screen != null) continue;
                if (EclipseData.getMutationLevel(client.player) < 1) {
                    client.player.displayClientMessage(Component.literal("O ECLIPSE SYSTEM ainda esta dormente."), true);
                } else {
                    client.setScreen(new EclipseScreen());
                }
            }
        });

        // Fase 05: braco biomecanico
        EntityModelLayerRegistry.registerModelLayer(BioArmModel.LAYER, BioArmModel::createLayer);
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, helper, context) -> {
            if (entityRenderer instanceof PlayerRenderer playerRenderer) {
                helper.register(new BioArmLayer(playerRenderer, context.getModelSet()));
            }
        });

        // Fase 06: HUD e recarga informada pelo servidor
        HudRenderCallback.EVENT.register(EclipseHud::render);
        ClientPlayNetworking.registerGlobalReceiver(CooldownPayload.TYPE,
                (payload, context) -> ClientCombatState.setCooldown(payload.ability(), payload.ticks()));

        NivekEclipse.LOGGER.info("NIVEK: ECLIPSE cliente inicializado");
    }

    private static KeyMapping key(String name, int code) {
        return KeyBindingHelper.registerKeyBinding(new KeyMapping(
                name, InputConstants.Type.KEYSYM, code, "key.categories.nivekeclipse"));
    }

    private static void requestAbility(Minecraft client, int ability) {
        if (client.player == null || client.screen != null) return;
        if (EclipseData.getMutationLevel(client.player) < 1) {
            client.player.displayClientMessage(Component.literal("Voce ainda nao possui a mutacao."), true);
        } else {
            ClientPlayNetworking.send(new AbilityPayload(ability));
        }
    }
}
