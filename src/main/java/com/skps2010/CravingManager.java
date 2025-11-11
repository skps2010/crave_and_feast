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

    public static void rebuildAllowedFoods() {
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

    // 確保當前渴望存在；到期就重抽並通知
    public static CravingState.Entry ensureCraving(ServerPlayerEntity p){
        var s = p.getServer(); var w = p.getWorld();
        var st = CravingState.get(s);
        var e = st.get(p.getUuid());
        long now = w.getTime();
        if (e == null || now >= e.nextChangeTick()) {
            String id = Registries.ITEM.getId(pickRandomAllowed()).toString();
            long next = now + FDConfigs.CFG.cravingChangeInterval;
            e = new CravingState.Entry(id, next, 0);
            st.set(p.getUuid(), e);
            // 主動通知
            ServerPlayNetworking.send(p, new CravingPayload(id));
        }
        return e;
    }

    // 玩家吃東西時呼叫；若是渴望就遞增，達上限則立刻重抽並通知
    public static void onConsume(ServerPlayerEntity p, ItemStack stack){
        var e = ensureCraving(p);
        String id = Registries.ITEM.getId(stack.getItem()).toString();
        if (!e.itemId().equals(id)) return;

        var st = CravingState.get(p.getServer());
        int c = e.eatenInRound() + 1;
        st.set(p.getUuid(), e.withCount(c));

        if (c >= FDConfigs.CFG.cravingMaxCount) {
            long now = p.getWorld().getTime();
            String nextId = pickRandomAllowed().toString();
            long next = now + FDConfigs.CFG.cravingChangeInterval;
            st.set(p.getUuid(), new CravingState.Entry(nextId, next, 0));
            // 主動通知
            ServerPlayNetworking.send(p, new CravingPayload(nextId));
        }
    }

    public static boolean isCraving(ServerPlayerEntity p, Item item){
        var e = ensureCraving(p);
        return Registries.ITEM.getId(item).toString().equals(e.itemId());
    }

    public static String getCurrentCravingItem(MinecraftServer server, UUID uuid) {
        CravingState.Entry e = CravingState.get(server).get(uuid);
        if (e == null) return "";
        return e.itemId();
    }

    private static Item pickRandomAllowed() {
        if (allowedFoods.isEmpty()) rebuildAllowedFoods();
        List<Item> pool = allowedFoods;
        if (pool.isEmpty()) return null;
        int idx = ThreadLocalRandom.current().nextInt(pool.size());
        return pool.get(idx);
    }
}
