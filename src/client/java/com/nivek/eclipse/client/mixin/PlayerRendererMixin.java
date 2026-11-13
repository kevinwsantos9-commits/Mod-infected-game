package com.nivek.eclipse.client.mixin;

import com.nivek.eclipse.client.render.ArmTransformTracker;
import com.nivek.eclipse.client.render.EclipseRenderState;
import com.nivek.eclipse.data.EclipseData;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public class PlayerRendererMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/client/renderer/entity/state/PlayerRenderState;F)V",
            at = @At("TAIL"))
    private void nivekeclipse$extract(AbstractClientPlayer player, PlayerRenderState state, float partialTick, CallbackInfo ci) {
        int level = EclipseData.getMutationLevel(player);
        EclipseRenderState data = (EclipseRenderState) state;
        data.nivekeclipse$setMutation(level);
        data.nivekeclipse$setTransform(ArmTransformTracker.update(player, level, partialTick));
    }
}
