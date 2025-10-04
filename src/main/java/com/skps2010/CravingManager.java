package com.skps2010;

import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.*;

/**
 * CravingManager
 * - 每個玩家維護一個「渴望食物」
 * - 每 30 分鐘更換一次 (預設 36000 ticks)
 */
public class CravingManager {
    private static final int CRAVING_INTERVAL_TICKS = 30 * 60 * 20; // 30 分鐘 (36000 ticks)
    private static final Random RANDOM = new Random();

    // 玩家 ID -> 渴望食物
    private static final Map<UUID, Item> cravings = new HashMap<>();
    // 玩家 ID -> 下次更新時間
    private static final Map<UUID, Long> nextUpdate = new HashMap<>();

    /**
     * 取得玩家目前渴望的食物
     */
    public static Item getCraving(ServerPlayerEntity player) {
        updateIfNeeded(player);
        return cravings.get(player.getUuid());
    }

    /**
     * 檢查並在需要時更換渴望食物
     */
    private static void updateIfNeeded(ServerPlayerEntity player) {
        long currentTick = player.getServerWorld().getTime();
        long scheduled = nextUpdate.getOrDefault(player.getUuid(), 0L);

        if (!cravings.containsKey(player.getUuid()) || currentTick >= scheduled) {
            Item newCraving = pickRandomFood();
            cravings.put(player.getUuid(), newCraving);
            nextUpdate.put(player.getUuid(), currentTick + CRAVING_INTERVAL_TICKS);

            System.out.println("[FoodDiversity] Player " + player.getName().getString()
                    + " now craves " + Registries.ITEM.getId(newCraving));
        }
    }

    /**
     * 從所有合法食物中挑一個 (排除有負面效果的)
     */
    private static Item pickRandomFood() {
        List<Item> validFoods = new ArrayList<>();
        for (Item item : Registries.ITEM) {
            if (item.getFoodComponent() != null && isValidFood(item)) {
                validFoods.add(item);
            }
        }
        if (validFoods.isEmpty()) {
            return Items.BREAD; // fallback
        }
        return validFoods.get(RANDOM.nextInt(validFoods.size()));
    }

    /**
     * 判斷是否是合法食物 (排除有負面效果的)
     */
    private static boolean isValidFood(Item item) {
        FoodComponent comp = item.getFoodComponent();
        if (comp == null) return false;
        return comp.getStatusEffects().stream().noneMatch(e -> e.getFirst().isNegative());
    }
}
