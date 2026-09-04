package me.wuntare.tradeautomat.registry;

import me.wuntare.tradeautomat.Main;
import me.wuntare.tradeautomat.gui.AutomatStorageMenu;
import me.wuntare.tradeautomat.gui.AutomatTradeSetupMenu;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.MenuType;

public final class ModMenuTypes {
    public static final MenuType<AutomatStorageMenu> AUTOMAT_STORAGE_MENU = Registry.register(
            BuiltInRegistries.MENU,
            Identifier.fromNamespaceAndPath(Main.MOD_ID, "automat_storage_menu"),
            new ExtendedMenuType<>(AutomatStorageMenu::new, BlockPos.STREAM_CODEC)
    );
    public static final MenuType<AutomatTradeSetupMenu> AUTOMAT_TRADE_SETUP_MENU = Registry.register(
            BuiltInRegistries.MENU,
            Identifier.fromNamespaceAndPath(Main.MOD_ID, "automat_trade_setup_menu"),
            new ExtendedMenuType<>(AutomatTradeSetupMenu::new, BlockPos.STREAM_CODEC)
    );

    private ModMenuTypes() {
    }

    public static void initialize() {
    }
}
