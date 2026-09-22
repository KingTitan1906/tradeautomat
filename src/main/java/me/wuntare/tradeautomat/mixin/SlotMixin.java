package me.wuntare.tradeautomat.mixin;

import me.wuntare.tradeautomat.block.RestrictedBlockItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class SlotMixin {

    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
    private void tradeautomat$preventPlacementInContainers(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        Slot slot = (Slot) (Object) this;

        if (stack.getItem() instanceof RestrictedBlockItem && !(slot.container instanceof Inventory)) {
            cir.setReturnValue(false);
        }
    }
}