package com.skps2010;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.server.network.ServerPlayerEntity;

public class FoodDiversityMod implements ModInitializer {
    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playS2C().register(FoodHistoryPayload.ID, FoodHistoryPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(CravingPayload.ID, CravingPayload.CODEC);
        FDConfigs.load();
//        ServerTickEvents.END_SERVER_TICK.register(server -> {
//            long now = server.getOverworld().getTime();
//            if (now % 20 != 0) return; // 每 20 tick 檢一次
//            for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
//                CravingManager.tickCraving(p, now);
//            }
//        });
        System.out.println("[FoodDiversityMod] Initialized - all foods now restore 6 hunger!");
    }
}
