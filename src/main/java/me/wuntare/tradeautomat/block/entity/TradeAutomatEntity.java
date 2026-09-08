package me.wuntare.tradeautomat.block.entity;

import me.wuntare.tradeautomat.gui.AutomatStorageMenu;
import me.wuntare.tradeautomat.model.TradeOffer;
import me.wuntare.tradeautomat.registry.ModBlockEntities;
import me.wuntare.tradeautomat.registry.ModDataComponents;
import me.wuntare.tradeautomat.registry.ModItems;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TradeAutomatEntity extends BlockEntity implements ExtendedMenuProvider<BlockPos> {
    private static final int MODULES_SLOT_COUNT = 18;
    private static final int BASE_SLOT_COUNT = 4;
    private static final int MAX_SLOT_COUNT = 148;
    private static final int BASE_TRADE_OFFER_COUNT = 2;
    private static final int MAX_TRADE_OFFER_COUNT = 37;
    private static final int MIN_TRADE_INPUT_SLOTS = TradeOffer.MIN_INPUTS;
    private static final int MAX_TRADE_INPUT_SLOTS = TradeOffer.MAX_INPUTS;
    private static final int MIN_TRADE_OUTPUT_SLOTS = TradeOffer.MIN_OUTPUTS;
    private static final int MAX_TRADE_OUTPUT_SLOTS = TradeOffer.MAX_OUTPUTS;

    private int unlockedSlots = BASE_SLOT_COUNT;
    private int unlockedTradeOffers = BASE_TRADE_OFFER_COUNT;
    private int actualInputSlots = MIN_TRADE_INPUT_SLOTS;
    private int actualOutputSlots = MIN_TRADE_OUTPUT_SLOTS;

    private String code = "";
    private final SimpleContainer invModule = new SimpleContainer(MODULES_SLOT_COUNT) {
        @Override
        public void setChanged() {
            super.setChanged();
            TradeAutomatEntity.this.recalculateModules();
            TradeAutomatEntity.this.setChanged();
        }
    };
    private final SimpleContainer invInput = new SimpleContainer(MAX_SLOT_COUNT) {
        @Override
        public void setChanged() {
            super.setChanged();
            TradeAutomatEntity.this.setChanged();
        }
    };
    private final SimpleContainer invOutput = new SimpleContainer(MAX_SLOT_COUNT) {
        @Override
        public void setChanged() {
            super.setChanged();
            TradeAutomatEntity.this.setChanged();
        }
    };
    private final List<TradeOffer> trades = new ArrayList<>();

    public TradeAutomatEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TRADE_AUTOMAT_ENTITY, pos, state);
    }

    public void recalculateModules() {
        int oldUnlockedSlots = this.unlockedSlots;

        int storageBonus = 0;
        int offerBonus = 0;
        int maxTradeModuleLevel = 0;

        for (int i = 0; i < this.invModule.getContainerSize(); i++) {
            ItemStack stack = this.invModule.getItem(i);
            if (stack.isEmpty()) continue;

            if (stack.is(ModItems.MODULE_STORAGE)) {
                int level = stack.getOrDefault(ModDataComponents.MODULE_LEVEL, 1);
                if (level == 1) {
                    storageBonus += 1;
                } else if (level == 2) {
                    storageBonus += 2;
                } else if (level == 3) {
                    storageBonus += 4;
                } else if (level == 4) {
                    storageBonus += 8;
                }
            }
            else if (stack.is(ModItems.MODULE_OFFER)) {
                int level = stack.getOrDefault(ModDataComponents.MODULE_LEVEL, 1);
                if (level == 1) {
                    offerBonus += 1;
                } else if (level == 2) {
                    offerBonus += 2;
                }
            }
            else if (stack.is(ModItems.MODULE_TRADE)) {
                int level = stack.getOrDefault(ModDataComponents.MODULE_LEVEL, 1);
                if (level > maxTradeModuleLevel) {
                    maxTradeModuleLevel = level;
                }
            }
        }

        int newUnlockedSlots = Math.min(this.MAX_SLOT_COUNT, this.BASE_SLOT_COUNT + storageBonus);
        this.unlockedSlots = newUnlockedSlots;
        this.unlockedTradeOffers = Math.min(this.MAX_TRADE_OFFER_COUNT, this.BASE_TRADE_OFFER_COUNT + offerBonus);

        switch (maxTradeModuleLevel) {
            case 1 -> {
                this.actualInputSlots = 3;
                this.actualOutputSlots = 1;
            }
            case 2 -> {
                this.actualInputSlots = 4;
                this.actualOutputSlots = 2;
            }
            case 3 -> {
                this.actualInputSlots = 6;
                this.actualOutputSlots = 3;
            }
            case 4 -> {
                this.actualInputSlots = 7;
                this.actualOutputSlots = 4;
            }
            case 5 -> {
                this.actualInputSlots = MAX_TRADE_INPUT_SLOTS;
                this.actualOutputSlots = MIN_TRADE_OUTPUT_SLOTS;
            }
            default -> {
                this.actualInputSlots = MIN_TRADE_INPUT_SLOTS;
                this.actualOutputSlots = MIN_TRADE_OUTPUT_SLOTS;
            }
        }

        this.actualInputSlots = Math.clamp(this.actualInputSlots, MIN_TRADE_INPUT_SLOTS, MAX_TRADE_INPUT_SLOTS);
        this.actualOutputSlots = Math.clamp(this.actualOutputSlots, MIN_TRADE_OUTPUT_SLOTS, MAX_TRADE_OUTPUT_SLOTS);
        if (newUnlockedSlots < oldUnlockedSlots) {
            dropLockedSlotItems(newUnlockedSlots);
        }
    }

    private void dropLockedSlotItems(int fromIndex) {
        if (this.level == null || this.level.isClientSide()) return;

        boolean updated = false;

        updated |= clearAndDropContainer(this.invInput, fromIndex);
        updated |= clearAndDropContainer(this.invOutput, fromIndex);

        if (updated) {
            this.setChanged();
        }
    }
    private boolean clearAndDropContainer(Container container, int fromIndex) {
        boolean hasDropped = false;

        for (int i = fromIndex; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                net.minecraft.world.Containers.dropItemStack(
                        this.level,
                        this.worldPosition.getX() + 0.5,
                        this.worldPosition.getY() + 0.5,
                        this.worldPosition.getZ() + 0.5,
                        stack.copy()
                );
                container.setItem(i, ItemStack.EMPTY);
                hasDropped = true;
            }
        }

        return hasDropped;
    }

    public SimpleContainer getInputContainer() {
        return invInput;
    }

    public SimpleContainer getOutputContainer() {
        return invOutput;
    }

    public SimpleContainer getModuleContainer() {
        return invModule;
    }

    public int getActualInputSlots() {
        return this.actualInputSlots;
    }

    public int getActualOutputSlots() {
        return this.actualOutputSlots;
    }

    public boolean hasCode() {
        return !this.code.isEmpty();
    }
    public String getCode() {
        return this.code;
    }
    public void setCode(String code) {
        this.code = code;
        this.setChanged();
    }

    public int getUnlockedSlots() {
        return this.unlockedSlots;
    }
    public int getUnlockedTradeOffers() { return this.unlockedTradeOffers; }

    public void ensureTradesSize() {
        while (this.trades.size() < MAX_TRADE_OFFER_COUNT) {
            this.trades.add(new TradeOffer());
        }
    }

    public List<TradeOffer> getTrades() {
        return this.trades;
    }

    public void setTrades(List<TradeOffer> newTrades) {
        this.trades.clear();

        for (int i = 0; i < MAX_TRADE_OFFER_COUNT; i++) {
            if (i < newTrades.size() && newTrades.get(i) != null) {
                this.trades.add(newTrades.get(i).copy());
            } else {
                this.trades.add(new TradeOffer());
            }
        }

        this.setChanged();

        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public boolean hasProductInStock(ItemStack required) {
        int count = 0;
        for (int i = 0; i < this.invOutput.getContainerSize(); i++) {
            ItemStack stack = this.invOutput.getItem(i);
            if (ItemStack.isSameItemSameComponents(stack, required)) {
                count += stack.getCount();
                if (count >= required.getCount()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean depositPayment(ItemStack stackToAdd) {
        if (stackToAdd.isEmpty()) return true;

        for (int i = 0; i < this.unlockedSlots; i++) {
            ItemStack slot = this.invInput.getItem(i);
            if (ItemStack.isSameItemSameComponents(slot, stackToAdd)) {
                int max = Math.min(slot.getMaxStackSize(), this.invInput.getMaxStackSize());
                int transfer = Math.min(stackToAdd.getCount(), max - slot.getCount());
                if (transfer > 0) {
                    slot.grow(transfer);
                    stackToAdd.shrink(transfer);
                    if (stackToAdd.isEmpty()) {
                        this.setChanged();
                        return true;
                    }
                }
            }
        }

        for (int i = 0; i < this.unlockedSlots; i++) {
            ItemStack slot = this.invInput.getItem(i);
            if (slot.isEmpty()) {
                this.invInput.setItem(i, stackToAdd.copy());
                stackToAdd.setCount(0);
                this.setChanged();
                return true;
            }
        }

        this.setChanged();
        return stackToAdd.isEmpty();
    }
    public void extractProduct(ItemStack required) {
        int toRemove = required.getCount();
        for (int i = 0; i < this.invOutput.getContainerSize(); i++) {
            ItemStack stack = this.invOutput.getItem(i);
            if (ItemStack.isSameItemSameComponents(stack, required)) {
                int take = Math.min(toRemove, stack.getCount());
                stack.shrink(take);
                toRemove -= take;
                if (toRemove <= 0) break;
            }
        }
        this.setChanged();
    }
    public boolean canAcceptTradeInputs(TradeOffer offer) {

        List<ItemStack> simulated = new ArrayList<>();
        for (int i = 0; i < unlockedSlots; i++) {
            simulated.add(this.invInput.getItem(i).copy());
        }

        for (ItemStack input : offer.getInputs()) {
            if (input.isEmpty()) continue;

            int toInsert = input.getCount();

            for (ItemStack slotStack : simulated) {
                if (!slotStack.isEmpty() && ItemStack.isSameItemSameComponents(slotStack, input)) {
                    int space = slotStack.getMaxStackSize() - slotStack.getCount();
                    int inserted = Math.min(space, toInsert);
                    slotStack.grow(inserted);
                    toInsert -= inserted;
                    if (toInsert <= 0) break;
                }
            }

            if (toInsert > 0) {
                for (int i = 0; i < simulated.size(); i++) {
                    if (simulated.get(i).isEmpty()) {
                        int inserted = Math.min(input.getMaxStackSize(), toInsert);
                        simulated.set(i, input.copyWithCount(inserted));
                        toInsert -= inserted;
                        if (toInsert <= 0) break;
                    }
                }
            }

            if (toInsert > 0) {
                return false;
            }
        }

        return true;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        this.recalculateModules();
        return new AutomatStorageMenu(containerId, inventory, this);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Component getDisplayName() {
        return Component.nullToEmpty("Automat Storage");
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayer player) {
        return this.worldPosition;
    }

    public void saveToStack(ItemStack stack, HolderLookup.Provider registries) {
        CompoundTag tag = this.saveCustomOnly(registries);
        if (!tag.isEmpty()) {
            stack.set(DataComponents.BLOCK_ENTITY_DATA, TypedEntityData.of(this.getType(), tag));
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        output.putString("code", this.code);
        output.store("input_inv", ItemStack.OPTIONAL_CODEC.listOf(), this.invInput.getItems());
        output.store("output_inv", ItemStack.OPTIONAL_CODEC.listOf(), this.invOutput.getItems());
        output.store("module_inv", ItemStack.OPTIONAL_CODEC.listOf(), this.invModule.getItems());
        output.store("trades", TradeOffer.CODEC.listOf(), this.trades);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        this.code = input.getString("code").orElse("");
        input.read("input_inv", ItemStack.OPTIONAL_CODEC.listOf()).ifPresent(list -> {
            for (int i = 0; i < list.size() && i < this.invInput.getContainerSize(); i++) {
                this.invInput.setItem(i, list.get(i));
            }
        });

        input.read("output_inv", ItemStack.OPTIONAL_CODEC.listOf()).ifPresent(list -> {
            for (int i = 0; i < list.size() && i < this.invOutput.getContainerSize(); i++) {
                this.invOutput.setItem(i, list.get(i));
            }
        });

        input.read("module_inv", ItemStack.OPTIONAL_CODEC.listOf()).ifPresent(list -> {
            for (int i = 0; i < list.size() && i < this.invModule.getContainerSize(); i++) {
                this.invModule.setItem(i, list.get(i));
            }
        });

        input.read("trades", TradeOffer.CODEC.listOf()).ifPresent(list -> {
            this.trades.clear();
            this.trades.addAll(list);
        });

        ensureTradesSize();
        this.recalculateModules();
    }

    public boolean isEmpty() {
        return this.invInput.isEmpty() && this.invOutput.isEmpty() && this.invModule.isEmpty() && this.code.isEmpty() && this.trades.isEmpty();
    }
}
