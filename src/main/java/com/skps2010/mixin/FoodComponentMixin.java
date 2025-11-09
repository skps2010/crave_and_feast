package com.skps2010.mixin;

import com.skps2010.CravingManager;
import com.skps2010.FDConfigs;
import com.skps2010.FoodHistoryManager;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodComponent.class)
public abstract class FoodComponentMixin {

    @Unique
    private static float computeMultiplier(boolean craving, int eaten) {
        var C = FDConfigs.CFG;
        if (craving) return C.cravingMultiplier;

        for (var r : C.rules) {
            if (r.maxCount < 0 || eaten <= r.maxCount) return r.multiplier;
        }
        return 1.0f;
    }

    @Inject(method = "onConsume", at = @At("HEAD"), cancellable = true)
    private void modifyFoodEffects(World world, LivingEntity user, ItemStack stack,
                                   net.minecraft.component.type.ConsumableComponent consumable,
                                   CallbackInfo ci) {

        if (!(user instanceof PlayerEntity player)) return;

        // 先判斷「想吃」
        boolean craving = CravingManager.isCraving(player, stack.getItem());
        FoodComponent food = (FoodComponent)(Object)this;

        int eaten = FoodHistoryManager.recordAndGetCount(player, stack.getItem().toString());
        float multiplier = computeMultiplier(craving, eaten - 1);

        int nutrition = Math.round(food.nutrition() * multiplier);
        float saturation = food.saturation() * multiplier;

        player.getHungerManager().eat(new FoodComponent(nutrition, saturation, food.canAlwaysEat()));

        // 你原有的音效等照舊
        Random random = user.getRandom();
        world.playSound(null, user.getX(), user.getY(), user.getZ(),
                consumable.sound().value(), SoundCategory.NEUTRAL, 1.0F,
                random.nextTriangular(1.0F, 0.4F));
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS, 0.5F,
                MathHelper.nextBetween(random, 0.9F, 1.0F));

        ci.cancel();
    }
}
