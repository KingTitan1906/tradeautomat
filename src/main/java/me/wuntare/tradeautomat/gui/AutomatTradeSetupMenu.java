package me.wuntare.tradeautomat.gui;


import me.wuntare.tradeautomat.block.entity.TradeAutomatEntity;
import me.wuntare.tradeautomat.gui.util.GhostSlot;
import me.wuntare.tradeautomat.registry.ModMenuTypes;
import me.wuntare.tradeautomat.model.TradeOffer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.*;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class AutomatTradeSetupMenu extends AbstractContainerMenu {

    public static final int VISIBLE_ROWS = 4;
    private final TradeAutomatEntity blockEntity;
    private int scrollOffset = 0;

    public AutomatTradeSetupMenu(int containerId, Inventory playerInventory, TradeAutomatEntity blockEntity) {
        super(ModMenuTypes.AUTOMAT_TRADE_SETUP_MENU, containerId);
        this.blockEntity = blockEntity;

        for (int row = 0; row < VISIBLE_ROWS; row++) {
            int y = 18 + row * 20;
            this.addSlot(new TradeSetupGhostSlot(row, 0, 14, y));
            this.addSlot(new TradeSetupGhostSlot(row, 1, 32, y));
            this.addSlot(new TradeSetupGhostSlot(row, 2, 74, y));
        }

        int playerInvY = 114;
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, playerInvY + row * 18));
            }
        }
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, playerInvY + 58));
        }
    }
    public AutomatTradeSetupMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        this(containerId, playerInventory, (TradeAutomatEntity) playerInventory.player.level().getBlockEntity(pos));
    }

    @Override
    public void clicked(int slotId, int button, ContainerInput clickType, Player player) {
        if (slotId >= 0 && slotId < 12) {
            Slot slot = this.slots.get(slotId);
            if (slot instanceof TradeSetupGhostSlot ghostSlot) {
                GhostSlot.handleGhostClick(ghostSlot, getCarried(), button);
                return;
            }
        }
        super.clicked(slotId, button, clickType, player);
    }

    public void setScrollOffset(int scrollOffset) {
        this.scrollOffset = Math.max(0, scrollOffset);
    }

    public int getScrollOffset() {
        return scrollOffset;
    }

    public TradeAutomatEntity getBlockEntity() {
        return blockEntity;
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this.blockEntity, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    private class TradeSetupGhostSlot extends GhostSlot {
        private final int rowInView;
        private final int slotType;

        public TradeSetupGhostSlot(int rowInView, int slotType, int x, int y) {
            super(new SimpleContainer(1), 0, x, y);
            this.rowInView = rowInView;
            this.slotType = slotType;
        }

        private int getActualTradeIndex() {
            return scrollOffset + rowInView;
        }

        public boolean isLocked() {
            return getActualTradeIndex() >= blockEntity.getUnlockedTradeOffers();
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return !isLocked();
        }

        @Override
        public ItemStack getItem() {
            if (isLocked()) return ItemStack.EMPTY;

            int index = getActualTradeIndex();
            List<TradeOffer> trades = blockEntity.getTrades();
            if (index >= trades.size()) return ItemStack.EMPTY;

            TradeOffer offer = trades.get(index);
            return (slotType == 0) ? offer.getInput(0) : (slotType == 1) ? offer.getInput(1) : offer.getOutput(0);
        }

        @Override
        public void set(ItemStack stack) {
            if (isLocked()) return;

            int index = getActualTradeIndex();
            List<TradeOffer> trades = blockEntity.getTrades();

            while (trades.size() <= index) {
                trades.add(new TradeOffer());
            }

            TradeOffer offer = trades.get(index);
            if (slotType == 0) offer.setInput(0, stack);
            else if (slotType == 1) offer.setInput(1, stack);
            else if (slotType == 2) offer.setOutput(0, stack);

            blockEntity.setChanged();
        }
    }
}