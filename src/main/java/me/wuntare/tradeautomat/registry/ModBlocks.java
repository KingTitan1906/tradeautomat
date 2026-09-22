package me.wuntare.tradeautomat.registry;

import java.util.function.BiFunction;
import java.util.function.Function;

import me.wuntare.tradeautomat.Main;
import me.wuntare.tradeautomat.block.EngineeringTerminalBlock;
import me.wuntare.tradeautomat.block.RestrictedBlockItem;
import me.wuntare.tradeautomat.block.TradeAutomat;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ModBlocks {
    private static Block register(
            String name,
            Function<BlockBehaviour.Properties, Block> blockFactory,
            BlockBehaviour.Properties properties,
            BiFunction<Block, Item.Properties, BlockItem> itemFactory
    ) {
        Identifier id = Identifier.fromNamespaceAndPath(Main.MOD_ID, name);
        BlockItemId blockId = BlockItemId.create(id, id);
        Block block = Blocks.register(blockId.block(), blockFactory, properties);

        BlockItem blockItem = itemFactory.apply(block, new Item.Properties().useBlockDescriptionPrefix().setId(blockId.item()));
        Registry.register(BuiltInRegistries.ITEM, blockId.item(), blockItem);

        return block;
    }

    private static Block register(String name, Function<BlockBehaviour.Properties, Block> blockFactory, BlockBehaviour.Properties properties) {
        return register(name, blockFactory, properties, BlockItem::new);
    }

    public static final Block TRADE_AUTOMAT = register("trade_automat", TradeAutomat::new, BlockBehaviour.Properties.of().sound(SoundType.COPPER), (block, props) -> new RestrictedBlockItem(block, props.stacksTo(1)));
    public static final Block ENGINEERING_TERMINAL = register("engineering_terminal", EngineeringTerminalBlock::new, BlockBehaviour.Properties.of().sound(SoundType.WOOD));

    public static void initialize() {}
}
