package me.wuntare.tradeautomat.util;

import me.wuntare.tradeautomat.model.TradeOffer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TradeUtils {

    public static List<ItemStack> aggregateInputs(List<ItemStack> inputs) {
        List<ItemStack> aggregated = new ArrayList<>();
        for (ItemStack stack : inputs) {
            if (stack.isEmpty()) continue;
            boolean merged = false;
            for (ItemStack agg : aggregated) {
                if (ItemStack.isSameItemSameComponents(agg, stack)) {
                    agg.grow(stack.getCount());
                    merged = true;
                    break;
                }
            }
            if (!merged) {
                aggregated.add(stack.copy());
            }
        }
        return aggregated;
    }

    public static boolean hasEnoughItemsForOffer(Player player, TradeOffer offer) {
        List<ItemStack> aggregated = aggregateInputs(offer.getInputs());

        for (ItemStack req : aggregated) {
            int found = 0;
            for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
                if (!stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, req)) {
                    found += stack.getCount();
                }
            }
            if (found < req.getCount()) {
                return false;
            }
        }
        return true;
    }

    public static void removeTradeInputs(Player player, TradeOffer offer) {
        List<ItemStack> aggregated = aggregateInputs(offer.getInputs());

        for (ItemStack req : aggregated) {
            int toRemove = req.getCount();
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (!stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, req)) {
                    int take = Math.min(toRemove, stack.getCount());
                    stack.shrink(take);
                    toRemove -= take;
                    if (toRemove <= 0) break;
                }
            }
        }
    }
}