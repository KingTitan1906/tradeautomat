package me.wuntare.tradeautomat.block.entity;

import me.wuntare.tradeautomat.gui.EngineeringTerminalMenu;
import me.wuntare.tradeautomat.registry.ModBlockEntities;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class EngineeringTerminalBlockEntity extends BlockEntity implements ExtendedMenuProvider<BlockPos> {
    public static final int TOTAL_SLOTS_COUNT = 4;

    private String currentCode = "";

    private final SimpleContainer inventory = new SimpleContainer(TOTAL_SLOTS_COUNT) {
        @Override
        public void setChanged() {
            super.setChanged();
            EngineeringTerminalBlockEntity.this.setChanged();
        }
    };

    public EngineeringTerminalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENGINEERING_TERMINAL_ENTITY, pos, state);
    }

    public SimpleContainer getInventory() {
        return this.inventory;
    }

    public String getCurrentCode() {
        return this.currentCode;
    }

    public void setCurrentCode(String code) {
        this.currentCode = code != null ? code : "";
        this.setChanged();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new EngineeringTerminalMenu(containerId, inventory, this);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.tradeautomat.engineering_terminal");
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayer player) {
        return this.worldPosition;
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
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("code", this.currentCode);
        output.store("inventory", ItemStack.OPTIONAL_CODEC.listOf(), this.inventory.getItems());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.currentCode = input.getString("code").orElse("");

        input.read("inventory", ItemStack.OPTIONAL_CODEC.listOf()).ifPresent(list -> {
            for (int i = 0; i < list.size() && i < this.inventory.getContainerSize(); i++) {
                this.inventory.setItem(i, list.get(i));
            }
        });
    }
}