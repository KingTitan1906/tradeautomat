package me.wuntare.tradeautomat.mixin;

import me.wuntare.tradeautomat.block.RestrictedBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DecoratedPotBlock;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DecoratedPotBlock.class)
public class DecoratedPotBlockMixin {

    @Inject(
            method = "useItemOn",
            at = @At("HEAD"),
            cancellable = true
    )
    private void tradeautomat$preventRestrictedItemInPot(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult,
            CallbackInfoReturnable<InteractionResult> cir
    ) {
        if (stack.getItem() instanceof RestrictedBlockItem) {
            if (level.getBlockEntity(pos) instanceof DecoratedPotBlockEntity pot && pot.getTheItem().isEmpty()) {
                cir.setReturnValue(InteractionResult.PASS);
            }
        }
    }
}