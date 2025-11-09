package com.skps2010;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class FoodTooltipHandler implements ClientModInitializer {
    public static final List<String> HISTORY = new ArrayList<>();
    private static ItemStack current = ItemStack.EMPTY;
    private static long remaining = 0L; // ticks

    @Override
    public void onInitializeClient() {
        ItemTooltipCallback.EVENT.register(FoodTooltipHandler::onTooltip);

        ClientPlayNetworking.registerGlobalReceiver(FoodHistoryPayload.ID, (payload, context) ->
                context.client().execute(() -> {
                    HISTORY.clear();
                    HISTORY.addAll(payload.history());
                }));

        ClientPlayNetworking.registerGlobalReceiver(CravingPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                current = new ItemStack(payload.toItem());
                remaining = Math.max(0, payload.remainingTicks());
            });
        });
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.of("fooddiversity", "craving_hud"),
                FoodTooltipHandler::render
        );
    }

    public static int count(String foodId) {
        int count = 0;
        for (String f : HISTORY) {
            if (f.equals(foodId)) count++;
        }
        return count;
    }

    private static void onTooltip(ItemStack stack, Item.TooltipContext ctx, TooltipType type, List<Text> lines) {
        var fc = stack.get(DataComponentTypes.FOOD);
        if (fc == null) return;

        int count = count(stack.getItem().toString());
        boolean craving = current.getItem() == stack.getItem();

        int base = fc.nutrition();
        float m = craving ? 2f : count == 0 ? 2f : count < 8 ? 1.5f : 1f;
        int add  = Math.max(0, Math.round(base * m) - base);
        lines.add(Text.literal(combinedIcons(base, add)));


        if (craving) lines.add(Text.literal("§6Craving!"));
        else lines.add(Text.literal(count > 0 ? "§7Eaten " + count + " time" + (count > 1 ? "s" : "") : "§7Never eaten"));

    }

    private static String combinedIcons(int basePts, int addPts) {
        int totalPts  = basePts + addPts;
        int totalFull = totalPts / 2;
        int totalHalf = totalPts % 2;

        int baseFull = basePts / 2;

        int yellowFull = totalFull - baseFull;  // 前面白色完整圖示數
        StringBuilder sb = new StringBuilder();

        // 白色完整圖示
        if (baseFull > 0) {
            sb.append("§f");
            sb.append("\ueff4".repeat(baseFull));
        }
        // 黃色完整圖示（加成）
        if (yellowFull > 0) {
            sb.append("§6");
            sb.append("\ueff4".repeat(yellowFull));
        }
        // 半格：依是否屬於加成決定顏色
        if (totalHalf == 1) {
            sb.append(addPts > 0 ? "§6" : "§f");
            sb.append('\ueff3');
        }
        return sb.toString();
    }

    private static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        if (current.isEmpty()) return;

        var mc = MinecraftClient.getInstance();
        int x = 10, y = 10;

        // 畫出食物圖示
        ctx.drawItem(current, x, y);

        // 轉換剩餘秒數
        long secs = remaining / 20;
        long mm = secs / 60, ss = secs % 60;

        // 繪字
        ctx.drawText(mc.textRenderer, "渴望食物", x + 20, y + 2, 0xFFFFFFFF, true);
        ctx.drawText(mc.textRenderer, current.getName().getString(),
                x + 20, y + 12, 0xFFAAAAAA, true);
    }
}
