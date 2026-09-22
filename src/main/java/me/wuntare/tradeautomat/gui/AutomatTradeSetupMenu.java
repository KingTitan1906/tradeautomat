package me.wuntare.tradeautomat.gui;

import me.wuntare.tradeautomat.block.entity.TradeAutomatEntity;
import me.wuntare.tradeautomat.gui.util.GhostSlot;
import me.wuntare.tradeautomat.model.TradeOffer;
import me.wuntare.tradeautomat.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class AutomatTradeSetupMenu extends AbstractContainerMenu {

    public static final int VISIBLE_ROWS = 4;
    private final TradeAutomatEntity blockEntity;
    private int scrollOffset = 0;

    private int actualInputSlots = TradeOffer.MIN_INPUTS;
    private int actualOutputSlots = TradeOffer.MIN_OUTPUTS;

    public AutomatTradeSetupMenu(int containerId, Inventory playerInventory, TradeAutomatEntity blockEntity) {
        super(ModMenuTypes.AUTOMAT_TRADE_SETUP_MENU, containerId);
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

            for (int in = 0; in < TradeOffer.MAX_INPUTS; in++) {
                int x = 8 + in * 18;
                this.addSlot(new TradeSetupGhostSlot(row, false, in, x, y));
            }

            for (int out = 0; out < TradeOffer.MAX_OUTPUTS; out++) {
                int x = 16 + (TradeOffer.MAX_INPUTS * 18) + 12 + out * 18;
                this.addSlot(new TradeSetupGhostSlot(row, true, out, x, y));
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

    public AutomatTradeSetupMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        this(containerId, playerInventory, (TradeAutomatEntity) playerInventory.player.level().getBlockEntity(pos));
    }

    public int getActualInputSlots() {
        return blockEntity != null ? blockEntity.getActualInputSlots() : actualInputSlots;
    }

    public int getActualOutputSlots() {
        return blockEntity != null ? blockEntity.getActualOutputSlots() : actualOutputSlots;
    }

    public int getVisibleRowsCount() {
        if (blockEntity == null) return 1;
        int unlocked = blockEntity.getUnlockedTradeOffers();
        return Math.max(1, Math.min(VISIBLE_ROWS, unlocked));
    }

    public int getDynamicPanelHeight() {
        return 18 + getVisibleRowsCount() * 22 + 10 + 86;
    }

    @Override
    public void clicked(int slotId, int button, ContainerInput clickType, Player player) {
        if (slotId >= 0 && slotId < this.slots.size()) {
            Slot slot = this.slots.get(slotId);
            if (slot instanceof TradeSetupGhostSlot ghostSlot) {
                if (!ghostSlot.isLocked()) {
                    GhostSlot.handleGhostClick(ghostSlot, getCarried(), button);
                }
                return;
            }
        }
        super.clicked(slotId, button, clickType, player);
    }

    public void setScrollOffset(int scrollOffset) {
        int maxOffset = blockEntity != null ? Math.max(0, blockEntity.getUnlockedTradeOffers() - VISIBLE_ROWS) : 0;
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

    public class TradeSetupGhostSlot extends GhostSlot {
        private final int rowInView;
        private final boolean isOutput;
        private final int subIndex;

        public TradeSetupGhostSlot(int rowInView, boolean isOutput, int subIndex, int x, int y) {
            super(new SimpleContainer(1), 0, x, y);
            this.rowInView = rowInView;
            this.isOutput = isOutput;
            this.subIndex = subIndex;
        }

        private int getActualTradeIndex() {
            return scrollOffset + rowInView;
        }

        public boolean isLocked() {
            if (blockEntity == null) return true;

            if (getActualTradeIndex() >= blockEntity.getUnlockedTradeOffers()) return true;

            if (isOutput) {
                return subIndex >= getActualOutputSlots();
            } else {
                return subIndex >= getActualInputSlots();
            }
        }

        @Override
        public boolean isActive() {
            return !isLocked();
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
            if (trades == null || index >= trades.size()) return ItemStack.EMPTY;

            TradeOffer offer = trades.get(index);
            if (offer == null) return ItemStack.EMPTY;

            return isOutput ? offer.getOutput(subIndex) : offer.getInput(subIndex);
        }

        @Override
        public void set(ItemStack stack) {
            if (isLocked()) return;

            int index = getActualTradeIndex();
            List<TradeOffer> trades = blockEntity.getTrades();
            if (trades == null) return;

            while (trades.size() <= index) {
                trades.add(new TradeOffer());
            }

            TradeOffer offer = trades.get(index);
            if (isOutput) {
                offer.setOutput(subIndex, stack);
            } else {
                offer.setInput(subIndex, stack);
            }

            blockEntity.setChanged();

            if (blockEntity.getLevel() != null && !blockEntity.getLevel().isClientSide()) {
                blockEntity.getLevel().sendBlockUpdated(
                        blockEntity.getBlockPos(),
                        blockEntity.getBlockState(),
                        blockEntity.getBlockState(),
                        3
                );
            }
        }
    }
}