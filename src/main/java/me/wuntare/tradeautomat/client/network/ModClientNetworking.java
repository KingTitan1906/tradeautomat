package me.wuntare.tradeautomat.client.network;

import me.wuntare.tradeautomat.client.gui.AutomatCodeScreen;
import me.wuntare.tradeautomat.client.gui.AutomatHubScreen;
import me.wuntare.tradeautomat.network.OpenCodeScreenPayload;
import me.wuntare.tradeautomat.network.OpenHubScreenPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

public class ModClientNetworking {

    public static void registerReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(OpenCodeScreenPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                Minecraft.getInstance().setScreenAndShow(new AutomatCodeScreen(payload.pos()));
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(OpenHubScreenPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                Minecraft.getInstance().setScreenAndShow(new AutomatHubScreen(payload.pos()));
            });
        });
    }
}