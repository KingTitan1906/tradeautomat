package me.wuntare.tradeautomat.client;

import me.wuntare.tradeautomat.client.gui.AutomatStorageScreen;
import me.wuntare.tradeautomat.client.gui.AutomatTradeScreen;
import me.wuntare.tradeautomat.client.gui.AutomatTradeSetupScreen;
import me.wuntare.tradeautomat.client.gui.EngineeringTerminalScreen;
import me.wuntare.tradeautomat.client.network.ModClientNetworking;
import me.wuntare.tradeautomat.registry.ModMenuTypes;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;

public class MainClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MenuScreens.register(ModMenuTypes.AUTOMAT_STORAGE_MENU, AutomatStorageScreen::new);
        MenuScreens.register(ModMenuTypes.AUTOMAT_TRADE_SETUP_MENU, AutomatTradeSetupScreen::new);
        MenuScreens.register(ModMenuTypes.AUTOMAT_TRADE_MENU, AutomatTradeScreen::new);
        MenuScreens.register(ModMenuTypes.ENGINEERING_TERMINAL_MENU, EngineeringTerminalScreen::new);

        ModClientNetworking.registerReceivers();
    }
}