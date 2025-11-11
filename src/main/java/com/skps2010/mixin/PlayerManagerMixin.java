package com.skps2010.mixin;

import com.skps2010.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin
{
    @Inject(at = @At("TAIL"), method = "onPlayerConnect")
    private void onPlayerConnect(ClientConnection conn, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo info)
    {
        ServerPlayNetworking.send(player, new FoodHistoryPayload(player));
        ServerPlayNetworking.send(player, new CravingPayload(CravingManager.getCurrentCravingItem(player.getServer(), player.getUuid())));
    }
}
