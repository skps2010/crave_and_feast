package com.skps2010;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class FoodTooltipHandler implements ClientModInitializer {
    public static final List<String> HISTORY = new ArrayList<>();

    @Override
    public void onInitializeClient() {
        ItemTooltipCallback.EVENT.register(FoodTooltipHandler::onTooltip);

        ClientPlayNetworking.registerGlobalReceiver(FoodHistoryPayload.ID, (payload, context) ->
                context.client().execute(() -> {
                    HISTORY.clear();
                    HISTORY.addAll(payload.history());
                }));
    }

    public static int count(String foodId) {
        int count = 0;
        for (String f : HISTORY) {
            if (f.equals(foodId)) count++;
        }
        return count;
    }

    private static void onTooltip(ItemStack stack, Item.TooltipContext ctx, TooltipType type, List<Text> lines) {
        @Nullable ConsumableComponent foodComponent = stack.get(DataComponentTypes.CONSUMABLE);
        if (foodComponent == null) return;

        String foodId = stack.getItem().toString();

        int count = count(foodId);
        if (count > 0) {
            lines.add(Text.literal("§7Eaten " + count + " time" + (count > 1 ? "s" : "")));
        } else {
            lines.add(Text.literal("§7Never eaten"));
        }
    }
}
