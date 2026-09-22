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
import net.minecraft.world.Containers;
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
    private static final int BASE_SLOT_COUNT = 9;
    private static final int MAX_SLOT_COUNT = 153;
    private static final int BASE_TRADE_OFFER_COUNT = 1;
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
        public boolean canPlaceItem(int slot, ItemStack stack) {
            if (stack.isEmpty()) return true;
            return stack.is(ModItems.MODULE_STORAGE)
                    || stack.is(ModItems.MODULE_OFFER)
                    || stack.is(ModItems.MODULE_TRADE);
        }

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

            int rawLevel = stack.getOrDefault(ModDataComponents.MODULE_LEVEL, 1);

            if (stack.is(ModItems.MODULE_STORAGE)) {
                int level = Math.clamp(rawLevel, 1, 4);
                storageBonus += (1 << (level - 1));

            } else if (stack.is(ModItems.MODULE_OFFER)) {
                int level = Math.min(rawLevel, 2);
                offerBonus += level;

            } else if (stack.is(ModItems.MODULE_TRADE)) {
                int level = Math.min(rawLevel, 5);
                maxTradeModuleLevel = Math.max(maxTradeModuleLevel, level);
            }
        }

        this.unlockedSlots = Math.min(MAX_SLOT_COUNT, BASE_SLOT_COUNT + storageBonus);
        this.unlockedTradeOffers = Math.min(MAX_TRADE_OFFER_COUNT, BASE_TRADE_OFFER_COUNT + offerBonus);

        this.actualInputSlots = switch (maxTradeModuleLevel) {
            case 1 -> 3;
            case 2 -> 4;
            case 3 -> 6;
            case 4 -> 7;
            case 5 -> MAX_TRADE_INPUT_SLOTS;
            default -> MIN_TRADE_INPUT_SLOTS;
        };

        this.actualOutputSlots = switch (maxTradeModuleLevel) {
            case 1 -> 1;
            case 2 -> 2;
            case 3 -> 3;
            case 4 -> 4;
            case 5 -> MAX_TRADE_OUTPUT_SLOTS;
            default -> MIN_TRADE_OUTPUT_SLOTS;
        };

        this.actualInputSlots = Math.clamp(this.actualInputSlots, MIN_TRADE_INPUT_SLOTS, MAX_TRADE_INPUT_SLOTS);
        this.actualOutputSlots = Math.clamp(this.actualOutputSlots, MIN_TRADE_OUTPUT_SLOTS, MAX_TRADE_OUTPUT_SLOTS);

        if (this.unlockedSlots < oldUnlockedSlots) {
            dropLockedSlotItems(this.unlockedSlots);
        }
    }

    private void dropLockedSlotItems(int fromIndex) {
        if (this.level == null || this.level.isClientSide()) return;

        boolean dropped = clearAndDropContainer(this.invInput, fromIndex)
                | clearAndDropContainer(this.invOutput, fromIndex);

        if (dropped) {
            this.setChanged();
        }
    }

    private boolean clearAndDropContainer(Container container, int fromIndex) {
        boolean hasDropped = false;
        for (int i = fromIndex; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(this.level, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5, stack.copy());
                container.setItem(i, ItemStack.EMPTY);
                hasDropped = true;
            }
        }
        return hasDropped;
    }

    public boolean hasProductsInStock(List<ItemStack> requiredList) {
        List<ItemStack> aggregated = new ArrayList<>();
        for (ItemStack req : requiredList) {
            if (req.isEmpty()) continue;
            boolean merged = false;
            for (ItemStack agg : aggregated) {
                if (ItemStack.isSameItemSameComponents(agg, req)) {
                    agg.grow(req.getCount());
                    merged = true;
                    break;
                }
            }
            if (!merged) {
                aggregated.add(req.copy());
            }
        }

        for (ItemStack req : aggregated) {
            int count = 0;
            for (int i = 0; i < this.invOutput.getContainerSize(); i++) {
                ItemStack stack = this.invOutput.getItem(i);
                if (ItemStack.isSameItemSameComponents(stack, req)) {
                    count += stack.getCount();
                    if (count >= req.getCount()) break;
                }
            }
            if (count < req.getCount()) return false;
        }

        return true;
    }

    public boolean hasProductInStock(ItemStack required) {
        return hasProductsInStock(List.of(required));
    }

    public boolean hasValidTrades() {
        int maxCheck = Math.min(this.unlockedTradeOffers, this.trades.size());
        for (int i = 0; i < maxCheck; i++) {
            TradeOffer offer = this.trades.get(i);
            if (offer != null && offer.isValid() && offer.isActive()) {
                return true;
            }
        }
        return false;
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

            if (toInsert > 0) return false;
        }

        return true;
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
                        syncToClient();
                        return true;
                    }
                }
            }
        }

        for (int i = 0; i < this.unlockedSlots; i++) {
            if (this.invInput.getItem(i).isEmpty()) {
                this.invInput.setItem(i, stackToAdd.copy());
                stackToAdd.setCount(0);
                syncToClient();
                return true;
            }
        }

        syncToClient();
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
        syncToClient();
    }

    public SimpleContainer getInputContainer() { return invInput; }
    public SimpleContainer getOutputContainer() { return invOutput; }
    public SimpleContainer getModuleContainer() { return invModule; }

    public int getActualInputSlots() { return this.actualInputSlots; }
    public int getActualOutputSlots() { return this.actualOutputSlots; }
    public int getUnlockedSlots() { return this.unlockedSlots; }
    public int getUnlockedTradeOffers() { return this.unlockedTradeOffers; }

    public boolean hasCode() { return !this.code.isEmpty(); }
    public String getCode() { return this.code; }
    public void setCode(String code) {
        this.code = code;
        this.setChanged();
    }

    public void ensureTradesSize() {
        while (this.trades.size() < MAX_TRADE_OFFER_COUNT) {
            this.trades.add(new TradeOffer());
        }
    }

    public List<TradeOffer> getTrades() { return this.trades; }

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

    public boolean isEmpty() {
        return this.invInput.isEmpty() && this.invOutput.isEmpty() && this.invModule.isEmpty() && this.code.isEmpty() && this.trades.isEmpty();
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
        return Component.translatable("block.tradeautomat.trade_automat.storage");
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

    public void syncToClient() {
        if (this.level != null && !this.level.isClientSide()) {
            this.setChanged();
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
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
}