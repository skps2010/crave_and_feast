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
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class FoodTooltipHandler implements ClientModInitializer {
    private static ItemStack current = ItemStack.EMPTY;
    private static Map<String, FoodHistoryPayload.FoodInfo> map =
            Map.of("default", new FoodHistoryPayload.FoodInfo(1, "error"));

    @Override
    public void onInitializeClient() {
        ItemTooltipCallback.EVENT.register(FoodTooltipHandler::onTooltip);

        ClientPlayNetworking.registerGlobalReceiver(FoodHistoryPayload.ID, (payload, ctx) ->
                ctx.client().execute(() -> map = Map.copyOf(payload.map()))
        );

        ClientPlayNetworking.registerGlobalReceiver(CravingPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                current = new ItemStack(payload.toItem());
            });
        });
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.of("fooddiversity", "craving_hud"),
                FoodTooltipHandler::render
        );
    }

    public static FoodHistoryPayload.FoodInfo get(String id) {
        return map.getOrDefault(id, map.get("default"));
    }

    private static void onTooltip(ItemStack stack, Item.TooltipContext ctx, TooltipType type, List<Text> lines) {
        var fc = stack.get(DataComponentTypes.FOOD);
        if (fc == null) return;

        var info = get(stack.getItem().toString());

        int base = fc.nutrition(), mod = Math.round(base * info.multiplier());
        lines.add(Text.literal(combinedIcons(base, mod - base)));
        lines.add(Text.literal(info.display()));
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

        // 繪字
        ctx.drawText(mc.textRenderer, "渴望食物", x + 20, y + 2, 0xFFFFFFFF, true);
        ctx.drawText(mc.textRenderer, current.getName().getString(),
                x + 20, y + 12, 0xFFAAAAAA, true);
    }
}
