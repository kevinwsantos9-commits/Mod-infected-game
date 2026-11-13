package com.nivek.eclipse.client.render;

/** Dados extras do PlayerRenderState (implementada via mixin). */
public interface EclipseRenderState {
    int nivekeclipse$getMutation();

    void nivekeclipse$setMutation(int mutation);

    /** 0..1 durante a animacao de transformacao; 1 = concluida. */
    float nivekeclipse$getTransform();

    void nivekeclipse$setTransform(float transform);
}
