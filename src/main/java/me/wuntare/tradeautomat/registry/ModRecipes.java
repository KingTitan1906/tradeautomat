package me.wuntare.tradeautomat.registry;

import me.wuntare.tradeautomat.Main;
import me.wuntare.tradeautomat.recipe.CustomRecipeSync;
import me.wuntare.tradeautomat.recipe.EngineeringTerminalRecipe;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class ModRecipes {
    public static final RecipeSerializer<EngineeringTerminalRecipe> ENGINEERING_TERMINAL_SERIALIZER = Registry.register(
            BuiltInRegistries.RECIPE_SERIALIZER, Identifier.fromNamespaceAndPath(Main.MOD_ID, "engineering"),
            new RecipeSerializer<>(EngineeringTerminalRecipe.CODEC, EngineeringTerminalRecipe.STREAM_CODEC)
    );
    public static final RecipeType<EngineeringTerminalRecipe> ENGINEERING_TERMINAL_TYPE = Registry.register(BuiltInRegistries.RECIPE_TYPE,
            Identifier.fromNamespaceAndPath(Main.MOD_ID, "engineering"),
            new RecipeType<EngineeringTerminalRecipe>() {
                @Override
                public String toString() {
                    return "engineering";
                }
            }
    );

    public static void initialize() {
        CustomRecipeSync.register(ENGINEERING_TERMINAL_SERIALIZER);
    }
}