package com.skps2010;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Environment(EnvType.CLIENT)
public class FoodTooltipHandler implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ItemTooltipCallback.EVENT.register(FoodTooltipHandler::onTooltip);

        ClientPlayNetworking.registerGlobalReceiver(FoodHistoryPayload.ID, (payload, context) ->
                context.client().execute(() ->
                        context.client().player.setComponent(DataComponentTypes.CUSTOM_DATA, payload.nbt())));
    }

    private static void onTooltip(ItemStack stack, Item.TooltipContext tooltipContext, TooltipType tooltipType, List<Text> lines) {
        @Nullable ConsumableComponent foodComponent = stack.get(DataComponentTypes.CONSUMABLE);
        if (foodComponent == null) return;

        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;

        String foodId = stack.getItem().toString();

        int count = FoodHistoryManager.getCount(player, foodId);
        if (count > 0) {
            lines.add(Text.literal("§7Eaten " + count + " time" + (count > 1 ? "s" : "")));
        } else {
            lines.add(Text.literal("§7Never eaten"));
        }
    }
}
