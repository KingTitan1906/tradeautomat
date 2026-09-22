package me.wuntare.tradeautomat.compat.jei;

import me.wuntare.tradeautomat.Main;
import me.wuntare.tradeautomat.client.gui.EngineeringTerminalScreen;
import me.wuntare.tradeautomat.gui.EngineeringTerminalMenu;
import me.wuntare.tradeautomat.recipe.CustomRecipeSync;
import me.wuntare.tradeautomat.recipe.EngineeringTerminalRecipe;
import me.wuntare.tradeautomat.registry.ModBlocks;
import me.wuntare.tradeautomat.registry.ModMenuTypes;
import me.wuntare.tradeautomat.registry.ModRecipes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import java.util.List;

@mezz.jei.api.JeiPlugin
public class JeiPlugin implements IModPlugin {

    @Override
    public Identifier getPluginUid() {
        return Identifier.fromNamespaceAndPath(Main.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new EngineeringTerminalCategory(guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<RecipeHolder<EngineeringTerminalRecipe>> recipes = getRecipes();
        registration.addRecipes(EngineeringTerminalCategory.RECIPE_TYPE, recipes);
    }

    private List<RecipeHolder<EngineeringTerminalRecipe>> getRecipes() {
        Level level = Minecraft.getInstance().level;
        if (level == null) return List.of();

        return CustomRecipeSync.allOfType(level, ModRecipes.ENGINEERING_TERMINAL_TYPE).stream().toList();
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(
                EngineeringTerminalScreen.class,
                102, 33, 24, 17,
                EngineeringTerminalCategory.RECIPE_TYPE
        );
        IModPlugin.super.registerGuiHandlers(registration);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(
                EngineeringTerminalMenu.class,
                ModMenuTypes.ENGINEERING_TERMINAL_MENU,
                EngineeringTerminalCategory.RECIPE_TYPE,
                EngineeringTerminalMenu.SLOT_INPUT_BASE,
                3,
                36,
                36
        );
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(
                EngineeringTerminalCategory.RECIPE_TYPE,
                ModBlocks.ENGINEERING_TERMINAL
        );
    }
}