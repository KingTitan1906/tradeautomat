package me.wuntare.tradeautomat.recipe;

import net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.Collection;

public final class CustomRecipeSync {
    private CustomRecipeSync() {}

    public static void register(RecipeSerializer<?> serializer) {
        RecipeSynchronization.synchronizeRecipeSerializer(serializer);
    }

    public static <I extends RecipeInput, T extends Recipe<I>> Collection<RecipeHolder<T>> allOfType(Level level, RecipeType<T> type) {
        return level.recipeAccess().getSynchronizedRecipes().getAllOfType(type);
    }
}
