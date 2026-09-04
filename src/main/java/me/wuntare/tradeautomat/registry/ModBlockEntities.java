package me.wuntare.tradeautomat.registry;

import me.wuntare.tradeautomat.Main;
import me.wuntare.tradeautomat.block.entity.TradeAutomatEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {
    private static <T extends BlockEntity> BlockEntityType<T> register(String name, FabricBlockEntityTypeBuilder.Factory<? extends T> entityFactory, Block... blocks) {
    	Identifier id = Identifier.fromNamespaceAndPath(Main.MOD_ID, name);
	    return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id, FabricBlockEntityTypeBuilder.<T>create(entityFactory, blocks).build());
    }

    public static final BlockEntityType<TradeAutomatEntity> TRADE_AUTOMAT_ENTITY = register("trade_automat_entity", TradeAutomatEntity::new, ModBlocks.TRADE_AUTOMAT);

    public static void initialize() {}
}
