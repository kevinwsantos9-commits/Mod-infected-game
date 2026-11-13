package com.nivek.eclipse.client.render;

import com.nivek.eclipse.NivekEclipse;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

/** Geometria do braco biomecanico (coordenadas relativas ao ombro do braco direito). */
public final class BioArmModel {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(NivekEclipse.MOD_ID, "bio_arm"), "main");
    public static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(NivekEclipse.MOD_ID, "textures/entity/bio_arm.png");

    private BioArmModel() {}

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // luva/antebraco blindado
        root.addOrReplaceChild("sleeve",
                CubeListBuilder.create().texOffs(0, 0).addBox(-3.4F, 3.5F, -2.4F, 4.8F, 6.0F, 4.8F),
                PartPose.ZERO);
        // placa do ombro
        root.addOrReplaceChild("shoulder",
                CubeListBuilder.create().texOffs(0, 12).addBox(-3.5F, -2.5F, -2.5F, 5.0F, 3.0F, 5.0F),
                PartPose.ZERO);
        // garras (pivo no pulso)
        root.addOrReplaceChild("claws",
                CubeListBuilder.create().texOffs(20, 12)
                        .addBox(-2.5F, 0.0F, -2.0F, 1.0F, 3.0F, 1.0F)
                        .addBox(-1.0F, 0.0F, -2.0F, 1.0F, 3.5F, 1.0F)
                        .addBox(0.5F, 0.0F, -2.0F, 1.0F, 3.0F, 1.0F),
                PartPose.offset(0.0F, 12.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 32);
    }
}
