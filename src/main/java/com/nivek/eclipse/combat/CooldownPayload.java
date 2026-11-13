package com.nivek.eclipse.combat;

import com.nivek.eclipse.NivekEclipse;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Servidor -> cliente: a habilidade entrou em recarga por N ticks (so para o HUD). */
public record CooldownPayload(int ability, int ticks) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<CooldownPayload> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(NivekEclipse.MOD_ID, "cooldown"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CooldownPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, CooldownPayload::ability,
            ByteBufCodecs.VAR_INT, CooldownPayload::ticks,
            CooldownPayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
