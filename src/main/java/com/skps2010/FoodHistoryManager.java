package com.skps2010;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * FoodHistoryManager
 * - 管理玩家最近吃過的 32 種食物
 * - 提供查詢：某食物在最近紀錄裡出現幾次
 */
public class FoodHistoryManager {
    private static final String NBT_KEY = "fooddiversity_history";
    private static final int MAX_HISTORY = 32;

    /**
     * 紀錄玩家吃的食物
     */
    public static void recordFood(ServerPlayerEntity player, ItemStack food) {
        NbtCompound persistentData = player.getPersistentData();
        Deque<String> history = loadHistory(persistentData);

        String itemId = Item.getRawId(food.getItem()) + "";
        if (history.size() >= MAX_HISTORY) {
            history.removeFirst();
        }
        history.addLast(itemId);

        saveHistory(persistentData, history);
    }

    /**
     * 取得某食物在最近 32 次裡出現的次數
     */
    public static int getFoodCount(ServerPlayerEntity player, ItemStack food) {
        NbtCompound persistentData = player.getPersistentData();
        Deque<String> history = loadHistory(persistentData);

        String itemId = Item.getRawId(food.getItem()) + "";
        int count = 0;
        for (String id : history) {
            if (id.equals(itemId)) count++;
        }
        return count;
    }

    /**
     * 載入玩家的食物紀錄
     */
    private static Deque<String> loadHistory(NbtCompound data) {
        Deque<String> history = new ArrayDeque<>();
        if (data.contains(NBT_KEY)) {
            String[] parts = data.getString(NBT_KEY).split(",");
            for (String s : parts) {
                if (!s.isEmpty()) history.add(s);
            }
        }
        return history;
    }

    /**
     * 儲存玩家的食物紀錄
     */
    private static void saveHistory(NbtCompound data, Deque<String> history) {
        StringBuilder sb = new StringBuilder();
        for (String s : history) {
            sb.append(s).append(",");
        }
        data.putString(NBT_KEY, sb.toString());
    }

    /**
     * 把最近的紀錄整理成 Map<ItemId, 次數>
     */
    public static Map<String, Integer> getHistorySummary(ServerPlayerEntity player) {
        Map<String, Integer> summary = new HashMap<>();
        NbtCompound persistentData = player.getPersistentData();
        Deque<String> history = loadHistory(persistentData);

        for (String id : history) {
            summary.put(id, summary.getOrDefault(id, 0) + 1);
        }
        return summary;
    }
}
