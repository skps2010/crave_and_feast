package com.skps2010;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;

public class FoodDiversityMod implements ModInitializer {
    public static final String MODID = "fooddiversity";

    @Override
    public void onInitialize() {
        System.out.println("[FoodDiversity] Mod initialized!");

        // 註冊事件 (後續會實作)
        UseItemCallback.EVENT.register(FoodEventHandler::onUseItem);
        ItemTooltipCallback.EVENT.register(FoodTooltipHandler::onTooltip);
    }
}
