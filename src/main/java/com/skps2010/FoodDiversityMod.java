package com.skps2010;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class FoodDiversityMod implements ModInitializer {
    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playS2C().register(FoodHistoryPayload.ID, FoodHistoryPayload.CODEC);
        System.out.println("[FoodDiversityMod] Initialized - all foods now restore 6 hunger!");
    }
}
