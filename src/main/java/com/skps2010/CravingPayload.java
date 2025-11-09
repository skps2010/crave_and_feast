package com.skps2010;

import net.minecraft.item.Item;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public record CravingPayload(String itemId, long remainingTicks) implements CustomPayload {
    public static final Id<CravingPayload> ID = new Id<>(Identifier.of("fooddiversity", "craving_sync"));

    public static final PacketCodec<RegistryByteBuf, CravingPayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.STRING, CravingPayload::itemId,
                    PacketCodecs.VAR_LONG, CravingPayload::remainingTicks,
                    CravingPayload::new
            );

    @Override public Id<? extends CustomPayload> getId() { return ID; }

    public Item toItem() { return Registries.ITEM.get(Identifier.tryParse(itemId)); }
}
