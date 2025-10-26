package com.skps2010;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.LinkedList;
import java.util.List;

public class FoodHistoryManager {

    private static final String NBT_KEY = "FoodDiversityHistory";
    private static final int MAX_HISTORY = 32;

    /**
     * 記錄食物，並返回該食物目前的累計食用次數
     */
    public static int recordAndGetCount(PlayerEntity player, String foodId) {
        // 取得玩家的自訂資料區塊（CUSTOM_DATA）
        NbtComponent persistent = player.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
        NbtCompound nbt = persistent.copyNbt();

        // 取得或建立食物歷史紀錄
        NbtCompound historyNbt = nbt.getCompoundOrEmpty(NBT_KEY);

        // 將歷史列表存在一個 NbtList 中
        NbtList historyList = historyNbt.getListOrEmpty("history");
        List<String> foods = new LinkedList<>();

        for (int i = 0; i < historyList.size(); i++) {
            foods.add(historyList.getString(i, "none"));
        }

        // 新增目前食物紀錄
        foods.add(foodId);
        if (foods.size() > MAX_HISTORY) {
            foods = foods.subList(foods.size() - MAX_HISTORY, foods.size());
        }

        // 計算目前食物出現次數
        int count = 0;
        for (String f : foods) {
            if (f.equals(foodId)) count++;
        }

        // 寫回到 NBT
        NbtList newList = new NbtList();
        for (String f : foods) {
            newList.add(net.minecraft.nbt.NbtString.of(f));
        }
        historyNbt.put("history", newList);
        nbt.put(NBT_KEY, historyNbt);
        NbtComponent component = NbtComponent.of(nbt);

        // 更新玩家資料
        player.setComponent(DataComponentTypes.CUSTOM_DATA, component);

        // ✅ 若在伺服器端，將資料同步給 client
        if (player instanceof ServerPlayerEntity serverPlayer) {
            ServerPlayNetworking.send(serverPlayer, new FoodHistoryPayload(component));
        }

        return count;
    }

    public static int getCount(PlayerEntity player, String foodId) {
        NbtComponent persistent = player.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
        NbtCompound nbt = persistent.copyNbt();
        NbtCompound historyNbt = nbt.getCompoundOrEmpty(NBT_KEY);
        NbtList historyList = historyNbt.getListOrEmpty("history");

        int count = 0;
        for (int i = 0; i < historyList.size(); i++) {
            String f = historyList.getString(i, "none");
            if (f.equals(foodId)) count++;
        }
        return count;
    }
}
