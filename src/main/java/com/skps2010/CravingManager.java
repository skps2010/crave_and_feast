// CravingManager.java
package com.skps2010;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.ApplyEffectsConsumeEffect;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.StreamSupport;

public final class CravingManager {
    // 缓存可用食物
    private static volatile List<Item> allowedFoods = List.of();

    private CravingManager() {
    }

    /* ------------------------- 可食物池 ------------------------- */

    public static void rebuildAllowedFoods() {
        allowedFoods = StreamSupport.stream(Registries.ITEM.spliterator(), false)
                .filter(i -> i.getComponents().get(DataComponentTypes.FOOD) != null)
                .filter(i -> !hasNegativeEffect(i))
                .toList();
    }

    private static boolean hasNegativeEffect(Item item) {
        var cc = item.getComponents().get(DataComponentTypes.CONSUMABLE);
        if (cc == null) return false;
        for (var ce : cc.onConsumeEffects()) {
            if (ce instanceof ApplyEffectsConsumeEffect a)
                for (var inst : a.effects())
                    if (inst.getEffectType().value().getCategory() == StatusEffectCategory.HARMFUL)
                        return true;
        }
        return false;
    }

    private static Optional<String> pickRandomAllowedId() {
        if (allowedFoods.isEmpty()) rebuildAllowedFoods();
        var pool = allowedFoods;
        if (pool.isEmpty()) return Optional.empty();
        var it = pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
        return Optional.of(Registries.ITEM.getId(it).toString());
    }

    /* ------------------------- 核心流程 ------------------------- */

    // 確保有渴望；過期則換新並通知
    public static CravingState.Entry ensureCraving(ServerPlayerEntity p) {
        var st = CravingState.get(p.getServer());
        var now = p.getWorld().getTime();
        var e = st.get(p.getUuid());
        if (e == null || now >= e.nextChangeTick()) e = rerollCraving(p, now);
        return e;
    }

    // 吃東西時呼叫；命中渴望則遞增，上限即換新並通知
    public static void onConsume(ServerPlayerEntity p, ItemStack stack) {
        var e = ensureCraving(p);
        var id = Registries.ITEM.getId(stack.getItem()).toString();
        if (!id.equals(e.itemId())) return;

        var st = CravingState.get(p.getServer());
        var c = e.eatenInRound() + 1;
        st.set(p.getUuid(), new CravingState.Entry(e.itemId(), e.nextChangeTick(), c));

        if (c >= FDConfigs.CFG.cravingMaxCount) rerollCraving(p, p.getWorld().getTime());
    }

    public static boolean isCraving(ServerPlayerEntity p, Item item) {
        var e = ensureCraving(p);
        var id = Registries.ITEM.getId(item).toString();
        return id.equals(e.itemId());
    }

    public static String getCurrentCravingItem(MinecraftServer server, UUID uuid) {
        CravingState.Entry e = CravingState.get(server).get(uuid);
        if (e == null) return "";
        return e.itemId();
    }

    /* ------------------------- 換新＋通知 ------------------------- */

    private static CravingState.Entry rerollCraving(ServerPlayerEntity p, long now) {
        var st = CravingState.get(p.getServer());
        var id = pickRandomAllowedId().orElse("minecraft:bread"); // 安全後備
        var next = now + FDConfigs.CFG.cravingChangeInterval;

        var e = new CravingState.Entry(id, next, 0);
        st.set(p.getUuid(), e);

        // 主動通知（含剩餘時間）
        ServerPlayNetworking.send(p, new CravingPayload(id));
        return e;
    }

    public static void tickCraving(ServerPlayerEntity p, long now) {
        var st = CravingState.get(p.getServer());
        var e = st.get(p.getUuid());
        if (e == null || now >= e.nextChangeTick()) rerollCraving(p, now); // 內部會送 CravingPayload
    }
}
