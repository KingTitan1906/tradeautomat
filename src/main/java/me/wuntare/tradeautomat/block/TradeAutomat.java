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
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

public class TradeAutomat extends BaseEntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty OFFSET_X = IntegerProperty.create("offset_x", 0, 2);
    public static final IntegerProperty OFFSET_Y = IntegerProperty.create("offset_y", 0, 1);

    public TradeAutomat(Properties settings) {
        settings.noOcclusion();
        super(settings);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(OFFSET_X, 0)
                .setValue(OFFSET_Y, 0));
    }

    private Direction getStructureDirection(Direction facing) {
        return facing.getCounterClockWise();
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 0.8F;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        Direction dir = getStructureDirection(facing);

        for (int x = 0; x <= 2; x++) {
            for (int y = 0; y <= 1; y++) {
                BlockPos checkPos = pos.relative(dir, x).above(y);
                if (checkPos.equals(pos)) continue;

                BlockState existingState = level.getBlockState(checkPos);
                if (!existingState.canBeReplaced()) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(TradeAutomat::new);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OFFSET_X, OFFSET_Y);
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos worldPosition, BlockState blockState) {
        if (blockState.getValue(OFFSET_X) == 0 && blockState.getValue(OFFSET_Y) == 0) {
            return new TradeAutomatEntity(worldPosition, blockState);
        }
        return null;
    }

    public BlockPos getMasterPos(BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING);
        Direction dir = getStructureDirection(facing);

        int offsetX = state.getValue(OFFSET_X);
        int offsetY = state.getValue(OFFSET_Y);

        return pos.relative(dir.getOpposite(), offsetX).below(offsetY);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Level level = ctx.getLevel();
        BlockPos masterPos = ctx.getClickedPos();
        Direction facing = ctx.getHorizontalDirection().getOpposite();
        Direction dir = getStructureDirection(facing);

        for (int y = 0; y <= 1; y++) {
            for (int x = 0; x <= 2; x++) {
                if (x != 0 || y != 0) {
                    BlockPos targetPos = masterPos.relative(dir, x).above(y);
                    if (!level.getBlockState(targetPos).canBeReplaced(ctx)) {
                        return null;
                    }
                }
            }
        }

        return this.defaultBlockState()
                .setValue(FACING, facing)
                .setValue(OFFSET_X, 0)
                .setValue(OFFSET_Y, 0);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (!level.isClientSide()) {
            Direction facing = state.getValue(FACING);
            Direction dir = getStructureDirection(facing);

            TypedEntityData<?> entityData = stack.get(DataComponents.BLOCK_ENTITY_DATA);

            for (int x = 0; x <= 2; x++) {
                for (int y = 0; y <= 1; y++) {
                    BlockPos partPos = pos.relative(dir, x).above(y);
                    BlockState partState = this.defaultBlockState()
                            .setValue(FACING, facing)
                            .setValue(OFFSET_X, x)
                            .setValue(OFFSET_Y, y);

                    level.setBlock(partPos, partState, 3);

                    if (x == 0 && y == 0 && entityData != null) {
                        BlockEntity be = level.getBlockEntity(partPos);
                        if (be instanceof TradeAutomatEntity automatEntity) {
                            entityData.loadInto(automatEntity, level.registryAccess());
                            automatEntity.setChanged();
                        }
                    }
                }
            }
        }
    }

    private InteractionResult handle(Level level, BlockPos pos, BlockState state, Player player, ItemStack stack) {
        BlockPos masterPos = getMasterPos(pos, state);

        if (!(level.getBlockEntity(masterPos) instanceof TradeAutomatEntity te)) {
            return InteractionResult.PASS;
        }

        if (!te.hasCode()) {
            if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                ServerPlayNetworking.send(serverPlayer, new OpenCodeScreenPayload(masterPos));
            }
            return InteractionResult.SUCCESS;
        }

        if (stack.is(ModItems.PUNCH_CARD)) {
            String cardCode = stack.getOrDefault(ModDataComponents.CODE, "");
            if (te.getCode().equals(cardCode)) {
                if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                    ServerPlayNetworking.send(serverPlayer, new OpenHubScreenPayload(masterPos));
                }
                return InteractionResult.SUCCESS;
            }
        }

        if (!te.hasValidTrades()) {
            if (!level.isClientSide()) {
                player.sendOverlayMessage(Component.translatable("message.tradeautomat.no_trades")
                        .withStyle(ChatFormatting.RED));
            }
            return InteractionResult.SUCCESS;
        }

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new ExtendedMenuProvider<BlockPos>() {
                @Override
                public BlockPos getScreenOpeningData(ServerPlayer player) {
                    return masterPos;
                }

                @Override
                public Component getDisplayName() {
                    return Component.translatable("block.tradeautomat.trade_automat");
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
        InteractionResult result = handle(level, pos, state, player, stack);
        return result != InteractionResult.PASS ? result : super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        InteractionResult result = handle(level, pos, state, player, ItemStack.EMPTY);
        return result != InteractionResult.PASS ? result : super.useWithoutItem(state, level, pos, player, hit);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            Direction facing = state.getValue(FACING);
            Direction dir = getStructureDirection(facing);

            int offsetX = state.getValue(OFFSET_X);
            int offsetY = state.getValue(OFFSET_Y);

            BlockPos masterPos = pos.relative(dir.getOpposite(), offsetX).below(offsetY);

            BlockEntity blockEntity = level.getBlockEntity(masterPos);
            if (blockEntity instanceof TradeAutomatEntity automatEntity) {
                ItemStack dropStack = new ItemStack(this.asItem());

                automatEntity.saveToStack(dropStack, level.registryAccess());

                net.minecraft.world.entity.item.ItemEntity itemEntity = new net.minecraft.world.entity.item.ItemEntity(
                        level,
                        masterPos.getX() + 0.5,
                        masterPos.getY() + 0.5,
                        masterPos.getZ() + 0.5,
                        dropStack
                );
                itemEntity.setDefaultPickUpDelay();
                level.addFreshEntity(itemEntity);
            }

            for (int x = 0; x <= 2; x++) {
                for (int y = 0; y <= 1; y++) {
                    BlockPos targetPos = masterPos.relative(dir, x).above(y);
                    BlockState targetState = level.getBlockState(targetPos);

                    if (targetState.is(this)) {
                        level.setBlock(targetPos, Blocks.AIR.defaultBlockState(), 35);
                    }
                }
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }
}