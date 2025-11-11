package com.skps2010.mixin;

import com.skps2010.CravingManager;
import com.skps2010.FDConfigs;
import com.skps2010.FoodHistoryManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(net.minecraft.component.type.FoodComponent.class)
public abstract class FoodComponentMixin {

    @Unique
    private static float fd$mult(boolean craving, int eatenBefore) {
        var C = FDConfigs.CFG;
        if (craving) return C.cravingMultiplier;
        for (var r : C.rules)
            if (r.maxCount < 0 || eatenBefore <= r.maxCount) return r.multiplier;
        return 1f;
    }

    @Redirect(
            method = "onConsume",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/HungerManager;eat(Lnet/minecraft/component/type/FoodComponent;)V"
            )
    )
    private void fd$eatWithBoost(
            net.minecraft.entity.player.HungerManager hunger,
            net.minecraft.component.type.FoodComponent original,
            net.minecraft.world.World world,
            net.minecraft.entity.LivingEntity user,
            net.minecraft.item.ItemStack stack) {

        // 不是玩家就照原樣
        if (!(user instanceof net.minecraft.server.network.ServerPlayerEntity player)) {
            hunger.eat(original);
            return;
        }

        // 渴望輪計數/到期處理（內部會必要時換新並送封包）
        CravingManager.onConsume(player, stack);

        boolean craving = CravingManager.isCraving(player, stack.getItem());
        int eatenBefore = FoodHistoryManager.recordAndGetCount(player, stack.getItem().toString()) - 1;
        if (eatenBefore < 0) eatenBefore = 0;

        float m = fd$mult(craving, eatenBefore);
        if (m == 1f) {
            hunger.eat(original);
            return;
        }

        // 基於原本的 FoodComponent 建一個加成版
        var boosted = new net.minecraft.component.type.FoodComponent(
                Math.round(original.nutrition() * m),
                original.saturation() * m,
                original.canAlwaysEat()
        );
        hunger.eat(boosted);
    }
}
