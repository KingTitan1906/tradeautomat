package me.wuntare.tradeautomat.registry;

import me.wuntare.tradeautomat.Main;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModCreativeTabs {
    public static final CreativeModeTab TRADEAUTOMAT_TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(Main.MOD_ID, "creative_tab"),
            FabricCreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.PUNCH_CARD))
                    .title(Component.translatable("tradeautomat.tab"))
                    .displayItems(((parameters, output) -> {
                        output.accept(ModBlocks.TRADE_AUTOMAT);
                        output.accept(ModBlocks.ENGINEERING_TERMINAL);
                        output.accept(ModItems.PUNCH_CARD);
                        output.accept(ModItems.MODULE_OFFER);
                        output.accept(ModItems.MODULE_STORAGE);
                        output.accept(ModItems.MODULE_TRADE);
                    }))
                    .build());

    public static void initialize() {}
}