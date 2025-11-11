package com.skps2010;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public record FoodHistoryPayload(Map<String, FoodInfo> map) implements CustomPayload {
    public static final Id<FoodHistoryPayload> ID =
            new Id<>(Identifier.of("food_diversity", "food_history_sync"));
    private static final PacketCodec<RegistryByteBuf, FoodInfo> INFO_CODEC =
            PacketCodec.tuple(
                    PacketCodecs.FLOAT, FoodInfo::multiplier,
                    PacketCodecs.STRING, FoodInfo::display,
                    FoodInfo::new
            );
    public static final PacketCodec<RegistryByteBuf, FoodHistoryPayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.map(HashMap::new, PacketCodecs.STRING, INFO_CODEC),
                    FoodHistoryPayload::map,
                    FoodHistoryPayload::new
            );

    public FoodHistoryPayload(ServerPlayerEntity player) {
        this(makeMap(player));
    }

    private static Map<String, FoodInfo> makeMap(ServerPlayerEntity player) {
        var cfg = FDConfigs.CFG;
        FoodHistoryState state = FoodHistoryState.get(player.getEntityWorld().getServer());
        var history = state.getHistory(player);
        var cravingId = CravingManager.getCurrentCravingItem(player.getEntityWorld().getServer(), player.getUuid());

        // 統計各食物吃過次數
        Map<String, Long> counts = history.stream()
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

        Map<String, FoodInfo> map = new HashMap<>();

        // 已吃過：依規則決定 multiplier/display；若是渴望則覆蓋
        for (var e : counts.entrySet()) {
            String foodId = e.getKey();
            int count = e.getValue().intValue();

            float mult;
            String disp;
            if (foodId.equals(cravingId)) {
                mult = cfg.cravingMultiplier;
                disp = cfg.cravingDisplay;
            } else {
                // 套用 rules
                mult = 1.0f;
                disp = "";
                for (var r : cfg.rules) {
                    if (r.maxCount < 0 || count <= r.maxCount) {
                        mult = r.multiplier;
                        disp = r.display;
                        break;
                    }
                }
            }
            map.put(foodId, new FoodInfo(mult, disp));
        }

        // 渴望食物：若尚未吃過，仍加入（顯示 Craving!）
        if (cravingId != null) {
            map.put(cravingId, new FoodInfo(cfg.cravingMultiplier, cfg.cravingDisplay));
        }

        // default：未吃過食物使用第一條規則
        var first = cfg.rules.getFirst();
        map.put("default", new FoodInfo(first.multiplier, first.display));

        return map;
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public record FoodInfo(float multiplier, String display) {
    }
}


