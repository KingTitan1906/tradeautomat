package me.wuntare.tradeautomat.mixin;

import me.wuntare.tradeautomat.block.RestrictedBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.ShelfBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShelfBlockEntity.class)
public class ShelfBlockEntityMixin {

    @Inject(
            method = "swapItemNoUpdate(ILnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void tradeautomat$preventRestrictedItemInShelf(int slot, ItemStack heldItemStack, CallbackInfoReturnable<ItemStack> cir) {
        if (heldItemStack.getItem() instanceof RestrictedBlockItem) {
            cir.setReturnValue(heldItemStack);
        }
    }
}