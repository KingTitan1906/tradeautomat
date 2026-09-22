package me.wuntare.tradeautomat.registry;

import java.util.function.Function;

import me.wuntare.tradeautomat.Main;
import me.wuntare.tradeautomat.item.PunchCard;
import me.wuntare.tradeautomat.item.ModuleOffer;
import me.wuntare.tradeautomat.item.ModuleStorage;
import me.wuntare.tradeautomat.item.ModuleTrade;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public class ModItems {
    public static final Item MODULE_STORAGE = register("module_storage", ModuleStorage::new, new ModuleStorage.Properties());
    public static final Item MODULE_TRADE = register("module_trade", ModuleTrade::new, new ModuleTrade.Properties());
    public static final Item MODULE_OFFER = register("module_offer", ModuleOffer::new, new ModuleOffer.Properties());
    public static final Item PUNCH_CARD = register("punch_card", PunchCard::new, new PunchCard.Properties());

    private static Item register(String name, Function<Item.Properties, Item> itemFactory, Item.Properties settings) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Main.MOD_ID, name));
        Item item = itemFactory.apply(settings.setId(key));

        Registry.register(BuiltInRegistries.ITEM, key, item);

        return item;
    }

    public static void initialize() {}
}
