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
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AutomatTradeMenu extends AbstractContainerMenu {
    public static final int VISIBLE_ROWS = 4;
    private final TradeAutomatEntity blockEntity;
    private int scrollOffset = 0;

    private int actualInputSlots = TradeOffer.MIN_INPUTS;
    private int actualOutputSlots = TradeOffer.MIN_OUTPUTS;

    public AutomatTradeMenu(int containerId, Inventory playerInventory, TradeAutomatEntity blockEntity) {
        super(ModMenuTypes.AUTOMAT_TRADE_MENU, containerId);
        this.blockEntity = blockEntity;

        if (this.blockEntity != null) {
            this.blockEntity.recalculateModules();
        }

        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return blockEntity != null ? blockEntity.getActualInputSlots() : TradeOffer.MIN_INPUTS;
            }

            @Override
            public void set(int value) {
                actualInputSlots = value;
            }
        });

        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return blockEntity != null ? blockEntity.getActualOutputSlots() : TradeOffer.MIN_OUTPUTS;
            }

            @Override
            public void set(int value) {
                actualOutputSlots = value;
            }
        });

        int visibleRows = getVisibleRowsCount();

        for (int row = 0; row < visibleRows; row++) {
            int y = 18 + row * 22;

            for (int in = 0; in < Objects.requireNonNull(blockEntity).getActualInputSlots(); in++) {
                int x = 8 + in * 18;
                this.addSlot(new TradeDisplaySlot(row, false, in, x, y));
            }

            for (int out = 0; out < blockEntity.getActualOutputSlots(); out++) {
                int x = 8 + (blockEntity.getActualInputSlots() * 18) + 26 + out * 18;
                this.addSlot(new TradeDisplaySlot(row, true, out, x, y));
            }
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

    public int getActualInputSlots() {
        return blockEntity != null ? blockEntity.getActualInputSlots() : actualInputSlots;
    }

    public int getActualOutputSlots() {
        return blockEntity != null ? blockEntity.getActualOutputSlots() : actualOutputSlots;
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
        return getValidTradeIndices().size();
    }

    public int getVisibleRowsCount() {
        return Math.max(1, Math.min(VISIBLE_ROWS, getValidTradesCount()));
    }

    public int getDynamicPanelHeight() {
        return 18 + getVisibleRowsCount() * 22 + 10 + 86;
    }

    public int getRawTradeIndex(int visibleRowIndex) {
        if (this.blockEntity == null) return -1;

        List<Integer> validIndices = getValidTradeIndices();
        int targetIndex = this.scrollOffset + visibleRowIndex;

        if (targetIndex >= 0 && targetIndex < validIndices.size()) {
            return validIndices.get(targetIndex);
        }
        return -1;
    }

    public int getActualTradeIndex(int visibleRowIndex) {
        return getRawTradeIndex(visibleRowIndex);
    }

    public boolean isTradeExecutableByRawIndex(int rawTradeIndex, Player player) {
        if (blockEntity == null || rawTradeIndex < 0) return false;

        List<TradeOffer> trades = blockEntity.getTrades();
        if (rawTradeIndex >= trades.size()) return false;

        TradeOffer offer = trades.get(rawTradeIndex);
        if (offer == null || !offer.isValid() || !offer.isActive()) return false;

        for (ItemStack output : offer.getOutputs()) {
            if (!output.isEmpty() && !blockEntity.hasProductInStock(output)) {
                return false;
            }
        }

        if (!blockEntity.canAcceptTradeInputs(offer)) return false;

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
        private final boolean isOutput;
        private final int subIndex;

        public TradeDisplaySlot(int rowInView, boolean isOutput, int subIndex, int x, int y) {
            super(new SimpleContainer(1), 0, x, y);
            this.rowInView = rowInView;
            this.isOutput = isOutput;
            this.subIndex = subIndex;
        }

        public int getRawIndex() {
            return getRawTradeIndex(rowInView);
        }

        public boolean isSlotLocked() {
            int rawIndex = getRawIndex();
            if (rawIndex == -1) return true;

            if (isOutput) {
                return subIndex >= getActualOutputSlots();
            } else {
                return subIndex >= getActualInputSlots();
            }
        }

        @Override
        public boolean isActive() {
            return !isSlotLocked();
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
            if (isSlotLocked() || blockEntity == null) return ItemStack.EMPTY;

            int rawIndex = getRawIndex();
            if (rawIndex == -1) return ItemStack.EMPTY;

            List<TradeOffer> trades = blockEntity.getTrades();
            if (trades == null || rawIndex >= trades.size()) return ItemStack.EMPTY;

            TradeOffer offer = trades.get(rawIndex);
            if (offer == null) return ItemStack.EMPTY;

            return isOutput ? offer.getOutput(subIndex) : offer.getInput(subIndex);
        }
    }
}