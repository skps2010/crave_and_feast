package com.skps2010;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * FoodEventHandler
 * - 監聽玩家吃東西事件
 * - 呼叫 FoodHistoryManager 記錄
 * - 檢查 CravingManager 是否符合渴望
 */
public class FoodEventHandler {

    public static ActionResult onUseItem(PlayerEntity player, net.minecraft.world.World world,
                                         Hand hand, ItemStack stack) {
        if (world.isClient) {
            return ActionResult.PASS; // 客戶端不處理
        }

        // 只在伺服端處理，且要確定是吃食物
        if (player instanceof ServerPlayerEntity serverPlayer) {
            if (stack.isFood()) {
                // 紀錄食物
                FoodHistoryManager.recordFood(serverPlayer, stack);

                // 查詢吃過次數
                int count = FoodHistoryManager.getFoodCount(serverPlayer, stack);

                // 渴望檢查
                Item craving = CravingManager.getCraving(serverPlayer);
                boolean isCraving = (stack.getItem() == craving);

                // Debug 輸出
                System.out.println("[FoodDiversity] " + serverPlayer.getName().getString()
                        + " ate " + stack.getItem().toString()
                        + " | Count in history=" + count
                        + " | Craving=" + (isCraving ? "YES" : "NO"));
            }
        }

        return ActionResult.PASS;
    }
}
