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
    private static final int BASE_TRADE_OFFER_COUNT = 1;
    private static final int MAX_TRADE_OFFER_COUNT = 37;

    private int unlockedSlots = BASE_SLOT_COUNT;
    private int unlockedTradeOffers = BASE_TRADE_OFFER_COUNT;

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
    private List<TradeOffer> trades = new ArrayList<>();

    public TradeAutomatEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TRADE_AUTOMAT_ENTITY, pos, state);
    }

    public void recalculateModules() {
        int oldUnlockedSlots = this.unlockedSlots;

        int bonus = 0;
        for (int i = 0; i < this.invModule.getContainerSize(); i++) {
            ItemStack stack = this.invModule.getItem(i);
            if (stack.isEmpty()) continue;

            if (stack.is(ModItems.MODULE_STORAGE)) {
                int level = stack.getOrDefault(ModDataComponents.MODULE_LEVEL, 1);
                if (level == 1) {
                    bonus++;
                } else if (level == 2) {
                    bonus += 2;
                } else if (level == 3) {
                    bonus += 4;
                } else if (level == 4) {
                    bonus += 8;
                }
            }
        }
        int newUnlockedSlots = Math.min(this.MAX_SLOT_COUNT, this.BASE_SLOT_COUNT + bonus);
        this.unlockedSlots = newUnlockedSlots;

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

    public List<TradeOffer> getTrades() {
        return this.trades;
    }

    public void setTrades(List<TradeOffer> newTrades) {
        this.trades.clear();
        for (TradeOffer offer : newTrades) {
            this.trades.add(offer.copy());
        }
        this.setChanged();
    }

    public void addTrade(TradeOffer offer) {
        this.trades.add(offer);
        this.setChanged();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new AutomatStorageMenu(containerId, inventory, this);
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

        this.recalculateModules();
    }

    public boolean isEmpty() {
        return this.invInput.isEmpty() && this.invOutput.isEmpty() && this.invModule.isEmpty() && this.code.isEmpty() && this.trades.isEmpty();
    }
}
