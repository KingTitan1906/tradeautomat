package me.wuntare.tradeautomat.gui;

import me.wuntare.tradeautomat.block.entity.EngineeringTerminalBlockEntity;
import me.wuntare.tradeautomat.recipe.EngineeringTerminalRecipe;
import me.wuntare.tradeautomat.recipe.EngineeringTerminalRecipeInput;
import me.wuntare.tradeautomat.registry.ModMenuTypes;
import me.wuntare.tradeautomat.registry.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class EngineeringTerminalMenu extends AbstractContainerMenu {
    public static final int SLOT_INPUT_BASE = 0;
    public static final int SLOT_INPUT_MAT1 = 1;
    public static final int SLOT_INPUT_MAT2 = 2;
    public static final int SLOT_OUTPUT = 3;

    private static final int INV_START = 4;
    private static final int INV_END = 31;
    private static final int HOTBAR_START = 31;
    private static final int HOTBAR_END = 40;

    private final EngineeringTerminalBlockEntity blockEntity;
    private final SimpleContainer inventory;
    private final Player player;

    private final DataSlot requiresCodeSlot = DataSlot.standalone();

    public EngineeringTerminalMenu(int containerId, Inventory playerInventory, EngineeringTerminalBlockEntity blockEntity) {
        super(ModMenuTypes.ENGINEERING_TERMINAL_MENU, containerId);
        this.blockEntity = blockEntity;
        this.player = playerInventory.player;
        this.inventory = blockEntity != null ? blockEntity.getInventory() : new SimpleContainer(4);

        this.addDataSlot(this.requiresCodeSlot);

        this.addSlot(new Slot(this.inventory, SLOT_INPUT_BASE, 20, 31) {
            @Override
            public void setChanged() {
                super.setChanged();
                EngineeringTerminalMenu.this.slotsChanged(EngineeringTerminalMenu.this.inventory);
            }
        });

        this.addSlot(new Slot(this.inventory, SLOT_INPUT_MAT1, 45, 31) {
            @Override
            public void setChanged() {
                super.setChanged();
                EngineeringTerminalMenu.this.slotsChanged(EngineeringTerminalMenu.this.inventory);
            }
        });

        this.addSlot(new Slot(this.inventory, SLOT_INPUT_MAT2, 70, 31) {
            @Override
            public void setChanged() {
                super.setChanged();
                EngineeringTerminalMenu.this.slotsChanged(EngineeringTerminalMenu.this.inventory);
            }
        });

        this.addSlot(new Slot(this.inventory, SLOT_OUTPUT, 134, 31) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                EngineeringTerminalMenu.this.onTakeOutput(player, stack);
                super.onTake(player, stack);
            }
        });

        int playerInvY = 84;
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, playerInvY + row * 18));
            }
        }

        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, playerInvY + 58));
        }

        this.updateRecipeResult();
    }

    public EngineeringTerminalMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        this(containerId, playerInventory, (EngineeringTerminalBlockEntity) playerInventory.player.level().getBlockEntity(pos));
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
    }

    public void updateCodeFromClient(String newCode) {
        if (this.blockEntity != null) {
            this.blockEntity.setCurrentCode(newCode);
            if (!this.player.level().isClientSide()) {
                this.updateRecipeResult();
            }
        }
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        this.updateRecipeResult();
    }

    public void updateRecipeResult() {
        if (this.player.level().isClientSide()) {
            return;
        }

        ItemStack base = this.inventory.getItem(SLOT_INPUT_BASE);
        ItemStack mat1 = this.inventory.getItem(SLOT_INPUT_MAT1);
        ItemStack mat2 = this.inventory.getItem(SLOT_INPUT_MAT2);
        String code = this.blockEntity != null ? this.blockEntity.getCurrentCode() : "";

        if (base.isEmpty()) {
            this.requiresCodeSlot.set(0);
            this.getSlot(SLOT_OUTPUT).set(ItemStack.EMPTY);
            this.broadcastChanges();
            return;
        }

        ServerLevel serverLevel = (ServerLevel) this.player.level();
        EngineeringTerminalRecipeInput recipeInput = new EngineeringTerminalRecipeInput(base, mat1, mat2, code);

        var recipeHolder = serverLevel.recipeAccess().getRecipeFor(ModRecipes.ENGINEERING_TERMINAL_TYPE, recipeInput, serverLevel);

        if (recipeHolder.isPresent()) {
            EngineeringTerminalRecipe recipe = recipeHolder.get().value();
            boolean codeRequired = recipe.requires_code();
            this.requiresCodeSlot.set(codeRequired ? 1 : 0);

            if (!codeRequired || recipe.matchesCode(code)) {
                ItemStack resultStack = recipe.assemble(recipeInput);
                this.getSlot(SLOT_OUTPUT).set(resultStack);
            } else {
                this.getSlot(SLOT_OUTPUT).set(ItemStack.EMPTY);
            }
        } else {
            this.requiresCodeSlot.set(0);
            this.getSlot(SLOT_OUTPUT).set(ItemStack.EMPTY);
        }

        this.broadcastChanges();
    }

    private void onTakeOutput(Player player, ItemStack resultStack) {
        if (player.level().isClientSide()) return;

        ItemStack baseStack = this.inventory.getItem(SLOT_INPUT_BASE);
        if (!baseStack.isEmpty()) baseStack.shrink(1);

        ItemStack mat1Stack = this.inventory.getItem(SLOT_INPUT_MAT1);
        if (!mat1Stack.isEmpty()) mat1Stack.shrink(1);

        ItemStack mat2Stack = this.inventory.getItem(SLOT_INPUT_MAT2);
        if (!mat2Stack.isEmpty()) mat2Stack.shrink(1);

        this.inventory.setChanged();
        this.updateRecipeResult();
    }

    public boolean isCodeRequired() {
        return this.requiresCodeSlot.get() == 1;
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this.blockEntity, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            itemstack = stackInSlot.copy();

            if (index == SLOT_OUTPUT) {
                if (!this.moveItemStackTo(stackInSlot, INV_START, HOTBAR_END, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(stackInSlot, itemstack);
            }
            else if (index >= SLOT_INPUT_BASE && index <= SLOT_INPUT_MAT2) {
                if (!this.moveItemStackTo(stackInSlot, INV_START, HOTBAR_END, false)) {
                    return ItemStack.EMPTY;
                }
            }
            else {
                boolean moved = false;

                Slot baseSlot = this.slots.get(SLOT_INPUT_BASE);
                if (!baseSlot.hasItem()) {
                    if (this.moveItemStackTo(stackInSlot, SLOT_INPUT_BASE, SLOT_INPUT_BASE + 1, false)) {
                        moved = true;
                    }
                }

                if (!moved) {
                    if (this.moveItemStackTo(stackInSlot, SLOT_INPUT_MAT1, SLOT_INPUT_MAT2 + 1, false)) {
                        moved = true;
                    }
                }

                if (!moved) {
                    if (index >= INV_START && index < INV_END) {
                        if (!this.moveItemStackTo(stackInSlot, HOTBAR_START, HOTBAR_END, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (index >= HOTBAR_START && index < HOTBAR_END) {
                        if (!this.moveItemStackTo(stackInSlot, INV_START, INV_END, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
            }

            if (stackInSlot.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stackInSlot.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, stackInSlot);
        }

        return itemstack;
    }

    public EngineeringTerminalBlockEntity getBlockEntity() {
        return this.blockEntity;
    }
}