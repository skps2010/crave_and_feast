package com.skps2010;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public class FoodHistoryManager {

    public static int recordAndGetCount(PlayerEntity player, String foodId) {
        if (!(player instanceof ServerPlayerEntity sp)) return 0;
        FoodHistoryState state = FoodHistoryState.get(sp.getEntityWorld().getServer());
        int record = state.record(player, foodId);
        ServerPlayNetworking.send(sp, new FoodHistoryPayload(sp));
        return record;
    }
}
