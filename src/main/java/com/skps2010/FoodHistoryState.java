package com.skps2010;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Uuids;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import java.util.*;

public class FoodHistoryState extends PersistentState {
    // Codec: 用 UUID 當 key
    public static final Codec<FoodHistoryState> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.unboundedMap(Uuids.CODEC, Codec.STRING.listOf())
                    .optionalFieldOf("history", Map.of())
                    .forGetter(s -> s.history)
    ).apply(i, FoodHistoryState::new));
    public static PersistentStateType<FoodHistoryState> TYPE = new PersistentStateType<>(
            "food_history", FoodHistoryState::new, CODEC, DataFixTypes.SAVED_DATA_MAP_DATA
    );
    private final Map<UUID, List<String>> history = new HashMap<>();

    // 深拷貝確保可變
    private FoodHistoryState(Map<UUID, List<String>> history) {
        history.forEach((uuid, list) -> this.history.put(uuid, new ArrayList<>(list)));
    }

    public FoodHistoryState() {
    }

    public static FoodHistoryState get(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE);
    }

    /**
     * 新增紀錄並返回食用次數
     */
    public int record(PlayerEntity player, String foodId) {
        List<String> list = history.computeIfAbsent(player.getUuid(), k -> new ArrayList<>());

        list.add(foodId);
        if (list.size() > FDConfigs.CFG.recordingFoodCount) list.removeFirst();

        int count = 0;
        for (String f : list) if (f.equals(foodId)) count++;
        markDirty();
        return count;
    }

    public List<String> getHistory(PlayerEntity player) {
        List<String> list = history.get(player.getUuid());
        if (list == null) return Collections.emptyList();
        return new ArrayList<>(list);
    }

    public int getCount(PlayerEntity player, String foodId) {
        List<String> list = history.get(player.getUuid());
        if (list == null) return 0;
        int count = 0;
        for (String f : list) if (f.equals(foodId)) count++;
        return count;
    }
}
