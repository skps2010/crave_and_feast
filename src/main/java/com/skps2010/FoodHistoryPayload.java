package com.skps2010;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.List;

public record FoodHistoryPayload(List<String> history) implements CustomPayload {
    public static final Identifier ID_RAW = Identifier.of("food_diversity", "food_history_sync");
    public static final CustomPayload.Id<FoodHistoryPayload> ID = new CustomPayload.Id<>(ID_RAW);

    // 封包格式：List<String>
    public static final PacketCodec<RegistryByteBuf, FoodHistoryPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING.collect(PacketCodecs.toList()),
            FoodHistoryPayload::history,
            FoodHistoryPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
