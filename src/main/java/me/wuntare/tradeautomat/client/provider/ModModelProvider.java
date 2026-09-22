package me.wuntare.tradeautomat.client.provider;

import me.wuntare.tradeautomat.registry.ModBlocks;
import me.wuntare.tradeautomat.registry.ModItems;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplates;

public class ModModelProvider extends FabricModelProvider {
    public ModModelProvider(FabricPackOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators blockModelGenerators) {
        blockModelGenerators.createTrivialCube(ModBlocks.TRADE_AUTOMAT);
    }

    @Override
    public void generateItemModels(ItemModelGenerators itemModelGenerators) {
        itemModelGenerators.generateFlatItem(ModItems.MODULE_STORAGE, ModelTemplates.FLAT_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.MODULE_TRADE, ModelTemplates.FLAT_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.MODULE_OFFER, ModelTemplates.FLAT_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.PUNCH_CARD, ModelTemplates.FLAT_ITEM);
    }
}
