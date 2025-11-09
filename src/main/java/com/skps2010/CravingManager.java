// CravingManager.java
package com.skps2010;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.ApplyEffectsConsumeEffect;
import net.minecraft.item.consume.ConsumeEffect;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public final class CravingManager {
    private CravingManager() {}

    // 快取「沒有負面效果」的食物清單（資料包重載時記得重建）
    private static volatile List<Item> allowedFoods = List.of();

    public static void rebuildAllowedFoods(MinecraftServer server) {
        List<Item> list = new ArrayList<>();
        for (Item item : Registries.ITEM) {
            // 只要有 FoodComponent 才算食物（避免把藥水、其他物品塞進來）
            if (item.getComponents().get(DataComponentTypes.FOOD) == null) continue;

            if (!hasNegativeEffect(item)) {
                list.add(item);
            }
        }
        allowedFoods = List.copyOf(list);
    }

    private static boolean hasNegativeEffect(Item item) {
        ConsumableComponent cc = item.getComponents().get(DataComponentTypes.CONSUMABLE);
        if (cc == null) return false; // 沒有可食用效果就視為沒有負面效果

        // 走訪所有消耗效果，挑出會套用藥水效果的那種
        for (ConsumeEffect ce : cc.onConsumeEffects()) {
            if (ce instanceof ApplyEffectsConsumeEffect apply) {
                for (StatusEffectInstance inst : apply.effects()) {
                    StatusEffect effect = inst.getEffectType().value();
                    // 只排除「有害」效果；如果你想連中性也排除，改成 != BENEFICIAL
                    if (effect.getCategory() == StatusEffectCategory.HARMFUL) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isCraving( PlayerEntity player, Item item) {
        MinecraftServer server = player.getServer();
        if (server == null) return false;
        ensurePool(server);

        CravingState state = CravingState.get(server);
        long now = server.getOverworld().getTime(); // world time in ticks

        UUID uuid = player.getUuid();
        CravingState.Entry e = state.get(uuid);
        if (e == null || now >= e.nextChangeTick()) {
            // 需要抽新的想吃
            Item chosen = pickRandomAllowed();
            if (chosen != null) {
                String itemId = Registries.ITEM.getId(chosen).toString();
                state.set(uuid, new CravingState.Entry(itemId, now + FDConfigs.CFG.cravingChangeInterval));
                if (player instanceof ServerPlayerEntity spe) {
                    ServerPlayNetworking.send(spe, new CravingPayload(itemId, FDConfigs.CFG.cravingChangeInterval));
                }
                e = state.get(uuid);
            }
        }

        if (e == null) return false;
        Identifier id = Registries.ITEM.getId(item);
        return id.toString().equals(e.itemId());
    }

    public static String getCurrentCravingItem(MinecraftServer server, UUID uuid) {
        CravingState.Entry e = CravingState.get(server).get(uuid);
        if (e == null) return "";
        return e.itemId();
    }

    private static void ensurePool(MinecraftServer server) {
        if (allowedFoods.isEmpty()) rebuildAllowedFoods(server);
    }

    private static Item pickRandomAllowed() {
        List<Item> pool = allowedFoods;
        if (pool.isEmpty()) return null;
        int idx = ThreadLocalRandom.current().nextInt(pool.size());
        return pool.get(idx);
    }
}
