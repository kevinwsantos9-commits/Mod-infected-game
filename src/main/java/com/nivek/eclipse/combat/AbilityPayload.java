package com.nivek.eclipse.combat;

import com.nivek.eclipse.NivekEclipse;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Pedido do cliente para usar uma habilidade (o servidor valida tudo). */
public record AbilityPayload(int ability) implements CustomPacketPayload {
    public static final int BLADE = 1;
    public static final int CLAWS = 2;

    public static final CustomPacketPayload.Type<AbilityPayload> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(NivekEclipse.MOD_ID, "ability"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AbilityPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, AbilityPayload::ability, AbilityPayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
