package com.nivek.eclipse.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;

/** Desenha o braco biomecanico sobre o braco direito de jogadores com mutacao >= 1. */
public class BioArmLayer extends RenderLayer<PlayerRenderState, PlayerModel> {
    private final ModelPart root;
    private final ModelPart claws;

    public BioArmLayer(RenderLayerParent<PlayerRenderState, PlayerModel> parent, EntityModelSet modelSet) {
        super(parent);
        this.root = modelSet.bakeLayer(BioArmModel.LAYER);
        this.claws = root.getChild("claws");
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       PlayerRenderState state, float yRot, float xRot) {
        EclipseRenderState data = (EclipseRenderState) state;
        if (data.nivekeclipse$getMutation() < 1 || state.isInvisible) return;

        float transform = data.nivekeclipse$getTransform();
        float age = state.ageInTicks;

        // transformacao: o braco cresce com um pequeno "estouro" no fim
        float eased = 1.0F - (1.0F - transform) * (1.0F - transform);
        float grow = 0.15F + 0.85F * eased;
        // idle: pulso lento + dedos oscilando
        float pulse = (float) Math.sin(age * 0.15F) * 0.03F;
        // ataque: garras abrem durante o balanco do braco
        float spread = (float) Math.sin(Math.min(1.0F, Math.max(0.0F, state.attackTime)) * Math.PI);

        claws.xRot = 0.15F + (float) Math.sin(age * 0.1F) * 0.08F - spread * 0.9F;
        claws.zRot = (float) Math.sin(age * 0.07F) * 0.05F + spread * 0.2F;

        poseStack.pushPose();
        getParentModel().rightArm.translateAndRotate(poseStack);
        poseStack.scale(grow + pulse, 1.0F, grow + pulse);
        root.render(poseStack, buffer.getBuffer(RenderType.entityCutoutNoCull(BioArmModel.TEXTURE)),
                packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
