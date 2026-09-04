package me.wuntare.tradeautomat.registry;

import me.wuntare.tradeautomat.Main;
import me.wuntare.tradeautomat.client.gui.AutomatStorageMenu;
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

    private ModMenuTypes() {
    }

    public static void initialize() {
    }
}
