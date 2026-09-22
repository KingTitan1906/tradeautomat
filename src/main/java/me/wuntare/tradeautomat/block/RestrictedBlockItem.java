package me.wuntare.tradeautomat.block;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class RestrictedBlockItem extends BlockItem {

    public RestrictedBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());

        if (state.is(Blocks.DECORATED_POT) && context.getPlayer() != null && !context.getPlayer().isSecondaryUseActive()) {
            return InteractionResult.FAIL;
        }

        return super.useOn(context);
    }

    @Override
    public boolean canFitInsideContainerItems() {
        return false;
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
        if (action == ClickAction.SECONDARY) {
            String itemName = slot.getItem().getItem().getClass().getName().toLowerCase();
            if (itemName.contains("backpack") || itemName.contains("bag") || itemName.contains("bundle")) {
                return true;
            }
        }
        return super.overrideStackedOnOther(stack, slot, action, player);
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (action == ClickAction.SECONDARY) {
            String otherName = other.getItem().getClass().getName().toLowerCase();
            if (otherName.contains("backpack") || otherName.contains("bag") || otherName.contains("bundle")) {
                return true;
            }
        }
        return super.overrideOtherStackedOnMe(stack, other, slot, action, player, access);
    }
}