package me.wuntare.tradeautomat.gui.util;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class GhostSlot extends Slot {
    public GhostSlot(Container container, int index, int x, int y) {
        super(container, index, x, y);
    }

    @Override
    public boolean mayPickup(Player player) {
        return false;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return true;
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    public static boolean handleGhostClick(Slot slot, ItemStack carriedStack, int button) {
        if (slot instanceof GhostSlot) {
            if (carriedStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                int count = (button == 1) ? 1 : carriedStack.getCount();
                slot.set(carriedStack.copyWithCount(count));
            }
            return true;
        }
        return false;
    }
}