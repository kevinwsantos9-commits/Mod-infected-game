package com.nivek.eclipse.client.mixin;

import com.nivek.eclipse.client.render.EclipseRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PlayerRenderState.class)
public class PlayerRenderStateMixin implements EclipseRenderState {
    @Unique
    private int nivekeclipse$mutation = 0;
    @Unique
    private float nivekeclipse$transform = 1.0F;

    @Override
    public int nivekeclipse$getMutation() {
        return nivekeclipse$mutation;
    }

    @Override
    public void nivekeclipse$setMutation(int mutation) {
        this.nivekeclipse$mutation = mutation;
    }

    @Override
    public float nivekeclipse$getTransform() {
        return nivekeclipse$transform;
    }

    @Override
    public void nivekeclipse$setTransform(float transform) {
        this.nivekeclipse$transform = transform;
    }
}
