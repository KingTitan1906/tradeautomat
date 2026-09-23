package me.wuntare.tradeautomat.mixin;

import me.wuntare.tradeautomat.block.RestrictedBlockItem;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Container.class)
public interface ContainerMixin {

    @Inject(
            method = "canPlaceItem",
            at = @At("HEAD"),
            cancellable = true
    )
    default void tradeautomat$preventInExternalContainers(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.getItem() instanceof RestrictedBlockItem && !(this instanceof Inventory)) {
            cir.setReturnValue(false);
        }
    }
}