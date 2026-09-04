package me.wuntare.tradeautomat.client;

import me.wuntare.tradeautomat.client.gui.AutomatCodeScreen;
import me.wuntare.tradeautomat.client.network.ModClientNetworking;
import me.wuntare.tradeautomat.network.OpenCodeScreenPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import me.wuntare.tradeautomat.client.gui.AutomatStorageScreen;
import me.wuntare.tradeautomat.registry.ModItems;
import me.wuntare.tradeautomat.registry.ModMenuTypes;

public class MainClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MenuScreens.register(ModMenuTypes.AUTOMAT_STORAGE_MENU, AutomatStorageScreen::new);

        ModClientNetworking.registerReceivers();
    }
}