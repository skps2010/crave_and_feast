package com.skps2010;

import net.minecraft.component.type.NbtComponent;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record FoodHistoryPayload(NbtComponent nbt) implements CustomPayload {
    public static final Identifier FOOD_DIVERSITY_ID = Identifier.of("food_diversity", "food_history_sync");
    public static final CustomPayload.Id<FoodHistoryPayload> ID = new CustomPayload.Id<>(FOOD_DIVERSITY_ID);
    public static final PacketCodec<PacketByteBuf, FoodHistoryPayload> CODEC = PacketCodec.tuple(
            NbtComponent.PACKET_CODEC,
            FoodHistoryPayload::nbt,
            FoodHistoryPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
