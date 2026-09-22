package me.wuntare.tradeautomat;

import me.wuntare.tradeautomat.network.*;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.wuntare.tradeautomat.registry.*;

public class Main implements ModInitializer {
	public static final String MOD_ID = "tradeautomat";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
        ModDataComponents.initialize();
		ModItems.initialize();
		ModBlocks.initialize();
		ModBlockEntities.initialize();
        ModCreativeTabs.initialize();
		ModMenuTypes.initialize();
        ModNetworking.registerPackets();
        ModRecipes.initialize();

		LOGGER.info("TradeAutomat initialized");
	}
}
