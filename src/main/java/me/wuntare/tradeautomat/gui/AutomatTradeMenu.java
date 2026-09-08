package me.wuntare.tradeautomat.gui;

import me.wuntare.tradeautomat.block.entity.TradeAutomatEntity;
import me.wuntare.tradeautomat.model.TradeOffer;
import me.wuntare.tradeautomat.registry.ModMenuTypes;
import me.wuntare.tradeautomat.util.TradeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AutomatTradeMenu extends AbstractContainerMenu {
    public static final int VISIBLE_ROWS = 4;
    private final TradeAutomatEntity blockEntity;
    private int scrollOffset = 0;

    public AutomatTradeMenu(int containerId, Inventory playerInventory, TradeAutomatEntity blockEntity) {
        super(ModMenuTypes.AUTOMAT_TRADE_MENU, containerId);
        this.blockEntity = blockEntity;

        if (this.blockEntity != null) {
            this.blockEntity.recalculateModules();
        }

        int visibleRows = getVisibleRowsCount();

        for (int row = 0; row < visibleRows; row++) {
            int y = 18 + row * 22;
            this.addSlot(new TradeDisplaySlot(row, 0, 14, y));
            this.addSlot(new TradeDisplaySlot(row, 1, 32, y));
            this.addSlot(new TradeDisplaySlot(row, 2, 74, y));
        }

        int playerInvY = 18 + visibleRows * 22 + 10;

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, playerInvY + row * 18));
            }
        }
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, playerInvY + 58));
        }
    }

    public AutomatTradeMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        this(containerId, playerInventory, (TradeAutomatEntity) playerInventory.player.level().getBlockEntity(pos));
    }

    public List<Integer> getValidTradeIndices() {
        List<Integer> validIndices = new ArrayList<>();
        if (this.blockEntity == null) return validIndices;

        List<TradeOffer> trades = this.blockEntity.getTrades();
        for (int i = 0; i < trades.size(); i++) {
            TradeOffer offer = trades.get(i);
            if (offer != null && offer.isValid()) {
                validIndices.add(i);
            }
        }
        return validIndices;
    }

    public int getValidTradesCount() {
        if (this.blockEntity == null) return 0;
        int count = 0;
        for (TradeOffer offer : this.blockEntity.getTrades()) {
            if (offer != null && offer.isValid()) {
                count++;
            }
        }
        return count;
    }

    public int getVisibleRowsCount() {
        return Math.min(VISIBLE_ROWS, getValidTradesCount());
    }

    public int getDynamicPanelHeight() {
        return 18 + getVisibleRowsCount() * 22 + 10 + 86;
    }

    public int getActualTradeIndex(int visibleRowIndex) {
        int actualIndex = this.scrollOffset + visibleRowIndex;
        if (this.blockEntity != null && actualIndex >= 0 && actualIndex < getValidTradesCount()) {
            return actualIndex;
        }
        return -1;
    }

    public boolean isTradeExecutableByRawIndex(int rawTradeIndex, Player player) {
        if (blockEntity == null || rawTradeIndex < 0) return false;

        List<TradeOffer> trades = blockEntity.getTrades();
        if (rawTradeIndex >= trades.size()) return false;

        TradeOffer offer = trades.get(rawTradeIndex);
        if (offer == null || !offer.isValid() || !offer.isActive()) return false;

        ItemStack reward = offer.getOutput(0);
        if (reward.isEmpty() || !blockEntity.hasProductInStock(reward)) return false;

        return TradeUtils.hasEnoughItemsForOffer(player, offer);
    }

    public void setScrollOffset(int scrollOffset) {
        int maxOffset = Math.max(0, getValidTradesCount() - VISIBLE_ROWS);
        this.scrollOffset = Math.min(Math.max(0, scrollOffset), maxOffset);
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

    public class TradeDisplaySlot extends Slot {
        private final int rowInView;
        private final int slotType;

        public TradeDisplaySlot(int rowInView, int slotType, int x, int y) {
            super(new SimpleContainer(1), 0, x, y);
            this.rowInView = rowInView;
            this.slotType = slotType;
        }

        public int getActualIndex() {
            return getActualTradeIndex(rowInView);
        }

        public boolean isActiveSlot() {
            return getActualIndex() != -1;
        }

        @Override
        public boolean isActive() {
            return getActualIndex() != -1;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }

        @Override
        public @NotNull ItemStack getItem() {
            int actualIndex = getActualIndex();
            if (actualIndex == -1 || blockEntity == null) return ItemStack.EMPTY;

            List<TradeOffer> trades = blockEntity.getTrades();
            if (trades == null || actualIndex >= trades.size()) return ItemStack.EMPTY;

            TradeOffer offer = trades.get(actualIndex);
            if (offer == null) return ItemStack.EMPTY;

            return (slotType == 0) ? offer.getInput(0) : (slotType == 1) ? offer.getInput(1) : offer.getOutput(0);
        }
    }
}