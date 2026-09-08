package me.wuntare.tradeautomat.block;

import com.mojang.serialization.MapCodec;
import me.wuntare.tradeautomat.block.entity.TradeAutomatEntity;
import me.wuntare.tradeautomat.gui.AutomatTradeMenu;
import me.wuntare.tradeautomat.network.OpenCodeScreenPayload;
import me.wuntare.tradeautomat.network.OpenHubScreenPayload;
import me.wuntare.tradeautomat.registry.ModDataComponents;
import me.wuntare.tradeautomat.registry.ModItems;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

public class TradeAutomat extends BaseEntityBlock {
    public TradeAutomat(Properties settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() { return simpleCodec(TradeAutomat::new); }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos worldPosition, BlockState blockState) { return new TradeAutomatEntity(worldPosition, blockState); }

    private InteractionResult handle(Level level, BlockPos pos, Player player, ItemStack stack) {
        if (!(level.getBlockEntity(pos) instanceof TradeAutomatEntity te)) {
            return InteractionResult.PASS;
        }

        if (!te.hasCode()) {
            if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                ServerPlayNetworking.send(serverPlayer, new OpenCodeScreenPayload(pos));
            }
            return InteractionResult.SUCCESS;
        }

        if (stack.is(ModItems.KEYCARD)) {
            String cardCode = stack.getOrDefault(ModDataComponents.CODE, "");
            if (te.getCode().equals(cardCode)) {
                if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                    ServerPlayNetworking.send(serverPlayer, new OpenHubScreenPayload(pos));
                }
                return InteractionResult.SUCCESS;
            }
        }

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new ExtendedMenuProvider<BlockPos>() {
                @Override
                public BlockPos getScreenOpeningData(ServerPlayer player) {
                    return pos;
                }

                @Override
                public Component getDisplayName() {
                    return Component.literal("Trade Automat");
                }

                @Override
                public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
                    return new AutomatTradeMenu(containerId, inventory, te);
                }
            });
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected @NotNull InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        InteractionResult result = handle(level, pos, player, stack);
        return result != InteractionResult.PASS ? result : super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        InteractionResult result = handle(level, pos, player, ItemStack.EMPTY);
        return result != InteractionResult.PASS ? result : super.useWithoutItem(state, level, pos, player, hit);
    }

    @Override
    public @NotNull BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TradeAutomatEntity te) {
            if (!level.isClientSide()) {
                if (player.isCreative() && !te.isEmpty()) {
                    ItemStack stack = new ItemStack(this);

                    te.saveToStack(stack, level.registryAccess());

                    ItemEntity itemEntity = new ItemEntity(
                            level,
                            pos.getX() + 0.5,
                            pos.getY() + 0.5,
                            pos.getZ() + 0.5,
                            stack
                    );
                    itemEntity.setDefaultPickUpDelay();
                    level.addFreshEntity(itemEntity);
                }
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
