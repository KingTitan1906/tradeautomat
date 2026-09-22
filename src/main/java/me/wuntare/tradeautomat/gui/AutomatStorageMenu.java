package me.wuntare.tradeautomat.gui;

import me.wuntare.tradeautomat.block.entity.TradeAutomatEntity;
import me.wuntare.tradeautomat.item.ModuleStorage;
import me.wuntare.tradeautomat.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class AutomatStorageMenu extends AbstractContainerMenu {
    public static final int VANILLA_SLOT_COUNT = 36;

    private final TradeAutomatEntity blockEntity;
    private final DataSlot unlockedSlots = DataSlot.standalone();

    private final int inputStart;
    private final int inputEnd;
    private final int outputStart;
    private final int outputEnd;
    private final int moduleStart;
    private final int moduleEnd;

    public AutomatStorageMenu(int containerId, Inventory inventory, BlockPos blockPos) {
        this(containerId, inventory, inventory.player.level().getBlockEntity(blockPos));
    }

    public AutomatStorageMenu(int containerId, Inventory inventory, BlockEntity blockEntity) {
        super(ModMenuTypes.AUTOMAT_STORAGE_MENU, containerId);
        if (!(blockEntity instanceof TradeAutomatEntity te)) {
            throw new IllegalArgumentException("Block entity is not a TestBlockEntity");
        }
        this.blockEntity = te;
        this.addDataSlot(this.unlockedSlots);

        addPlayerHotbar(inventory);
        addPlayerInventory(inventory);

        Container input = this.blockEntity.getInputContainer();
        this.inputStart = this.slots.size();
        addStorage(input, 9, 17, -173, -64, true);
        this.inputEnd = this.slots.size();

        Container output = this.blockEntity.getOutputContainer();
        this.outputStart = this.slots.size();
        addStorage(output, 9, 17, 187, -64, true);
        this.outputEnd = this.slots.size();

        Container module = this.blockEntity.getModuleContainer();
        this.moduleStart = this.slots.size();
        addModuleStorage(module, 9, 2, 8, 34);
        this.moduleEnd = this.slots.size();
    }

    @Override
    public void broadcastChanges() {
        if (this.blockEntity.getLevel() != null && !this.blockEntity.getLevel().isClientSide()) {
            this.unlockedSlots.set(this.blockEntity.getUnlockedSlots());
        }
        super.broadcastChanges();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (slotIndex >= VANILLA_SLOT_COUNT) {
                if (!this.moveItemStackTo(itemstack1, 0, VANILLA_SLOT_COUNT, true)) {
                    return ItemStack.EMPTY;
                }
            }
            else {
                if (itemstack1.getItem() instanceof ModuleStorage) {
                    if (!this.moveItemStackTo(itemstack1, moduleStart, moduleEnd, false)) {
                        if (!this.moveItemStackTo(itemstack1, inputStart, outputEnd, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                } else {
                    if (!this.moveItemStackTo(itemstack1, inputStart, outputEnd, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemstack1);
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this.blockEntity, player);
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory inventory) {
        for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(inventory, column, 8 + column * 18, 142));
        }
    }

    private void addStorage(Container container, int columns, int rows, int xOffset, int yOffset, boolean useUnlocked) {
        int index = 0;
        int containerSize = container.getContainerSize();
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                if (index >= containerSize) return;
                final int slotIndex = index++;
                this.addSlot(new Slot(container, slotIndex, xOffset + column * 18, yOffset + row * 18) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        if (!useUnlocked) return true;
                        else {
                            return slotIndex < unlockedSlots.get();
                        }
                    }
                });
            }
        }
    }
    private void addModuleStorage(Container container, int columns, int rows, int xOffset, int yOffset) {
        int index = 0;
        int containerSize = container.getContainerSize();
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                if (index >= containerSize) return;
                final int slotIndex = index++;
                this.addSlot(new Slot(container, slotIndex, xOffset + column * 18, yOffset + row * 18) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return container.canPlaceItem(slotIndex, stack);
                    }
                });
            }
        }
    }

    public TradeAutomatEntity getBlockEntity() {
        return blockEntity;
    }
}