package com.skps2010;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.util.List;

/**
 * FoodTooltipHandler
 * - 在物品提示顯示：
 *   * 吃過次數 / 32
 *   * 效果倍率
 *   * 是否是渴望食物
 */
public class FoodTooltipHandler {
    public static void onTooltip(ItemStack stack, ItemTooltipCallback.Context context, List<Text> lines) {
        World world = context.getWorld();
        if (world == null || !stack.isFood()) return;

        // 嘗試取得玩家 (僅限本地玩家)
        if (!(context.getPlayer() instanceof ServerPlayerEntity player)) return;

        // 吃過次數
        int count = FoodHistoryManager.getFoodCount(player, stack);
        lines.add(Text.literal("Eaten: " + count + "/" + 32));

        // 計算倍率
        float multiplier = 1.0f;
        if (count == 0) {
            multiplier = 2.0f;
        } else if (count <= 8) {
            multiplier = 1.5f;
        } else {
            multiplier = 1.0f;
        }
        lines.add(Text.literal("Effect: " + multiplier + "x"));

        // 渴望檢查
        Item craving = CravingManager.getCraving(player);
        if (stack.getItem() == craving) {
            lines.add(Text.literal("§dCraving!")); // 紫色字
        }
    }
}
