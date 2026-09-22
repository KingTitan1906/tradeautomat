package me.wuntare.tradeautomat.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public record EngineeringTerminalRecipeInput(ItemStack base, ItemStack mat1, ItemStack mat2, String code) implements RecipeInput {
    @Override
    public ItemStack getItem(int index) {
        return switch (index) {
            case 0 -> base;
            case 1 -> mat1;
            case 2 -> mat2;
            default -> ItemStack.EMPTY;
        };
    }

    @Override
    public int size() {
        return 3;
    }
}