package com.skps2010.mixin;

import net.minecraft.entity.player.HungerManager;
import net.minecraft.component.type.FoodComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HungerManager.class)
public abstract class HungerManagerMixin {

    @Inject(method = "eat(Lnet/minecraft/component/type/FoodComponent;)V", at = @At("HEAD"), cancellable = true)
    private void modifyFoodValue(FoodComponent foodComponent, CallbackInfo ci) {
        // 呼叫 addInternal(6, foodComponent.saturation())
        HungerManager self = (HungerManager) (Object) this;
        try {
            var method = HungerManager.class.getDeclaredMethod("addInternal", int.class, float.class);
            method.setAccessible(true);
            method.invoke(self, 6, foodComponent.saturation());
        } catch (Exception e) {
            System.err.println("[FoodDiversityMod] Failed to modify hunger: " + e);
        }
        ci.cancel(); // 阻止原本的 eat() 執行
    }
}
