package me.wuntare.tradeautomat.compat.jei;

import me.wuntare.tradeautomat.Main;
import me.wuntare.tradeautomat.recipe.EngineeringTerminalRecipe;
import me.wuntare.tradeautomat.registry.ModBlocks;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;


public class EngineeringTerminalCategory implements IRecipeCategory<RecipeHolder<EngineeringTerminalRecipe>> {
    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableStatic slotDrawable;

    @SuppressWarnings("unchecked")
    public static final IRecipeType<RecipeHolder<EngineeringTerminalRecipe>> RECIPE_TYPE =
            IRecipeType.create(Main.MOD_ID, "engineering", (Class<RecipeHolder<EngineeringTerminalRecipe>>) (Class<?>) RecipeHolder.class);

    public EngineeringTerminalCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(150, 60);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.ENGINEERING_TERMINAL));
        this.slotDrawable = guiHelper.getSlotDrawable();
    }

    @Override
    public IRecipeType<RecipeHolder<EngineeringTerminalRecipe>> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, RecipeHolder<EngineeringTerminalRecipe> recipe, IFocusGroup focuses) {
        builder.addAnimatedRecipeArrow(200).setPosition(80, 8);
        IRecipeCategory.super.createRecipeExtras(builder, recipe, focuses);
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.tradeautomat.engineering_terminal");
    }



    @Override
    public int getWidth() {
        return 150;
    }

    @Override
    public int getHeight() {
        return 60;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<EngineeringTerminalRecipe> holder, IFocusGroup focuses) {
        EngineeringTerminalRecipe recipe = holder.value();

        builder.addInputSlot(10, 8)
                .setStandardSlotBackground()
                .addIngredients(VanillaTypes.ITEM_STACK, recipe.base().getMatchingStacksList());

        builder.addInputSlot(32, 8)
                .setStandardSlotBackground()
                .addIngredients(VanillaTypes.ITEM_STACK, recipe.mat1().getMatchingStacksList());

        if (recipe.mat2().isPresent()) {
            builder.addInputSlot(54, 8)
                    .setStandardSlotBackground()
                    .addIngredients(VanillaTypes.ITEM_STACK, recipe.mat2().get().getMatchingStacksList());
        }

        builder.addOutputSlot(118, 8)
                .setOutputSlotBackground()
                .add(recipe.result().create());
    }

    @Override
    public void draw(RecipeHolder<EngineeringTerminalRecipe> holder, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        EngineeringTerminalRecipe recipe = holder.value();

        this.slotDrawable.draw(guiGraphics, 9, 7);
        this.slotDrawable.draw(guiGraphics, 31, 7);
        if (recipe.mat2().isPresent()) {
            this.slotDrawable.draw(guiGraphics, 53, 7);
        }
        this.slotDrawable.draw(guiGraphics, 117, 7);

        Font font = Minecraft.getInstance().font;
        if (recipe.requires_code()) {
            Component codeText = Component.translatable("tradeautomat.recipe.code_display", recipe.min_code(), recipe.max_code());
            guiGraphics.text(font, codeText, 10, 42, 0xFF000000, false);
        } else {
            Component codeNotNeeded = Component.translatable("tradeautomat.recipe.code_not_needed");
            guiGraphics.text(font, codeNotNeeded, 10, 42, 0xFFAAAAAA, false);
        }
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, RecipeHolder<EngineeringTerminalRecipe> holder, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        EngineeringTerminalRecipe recipe = holder.value();

        if (mouseX >= 10 && mouseX <= 140 && mouseY >= 40 && mouseY <= 52) {
            if (recipe.requires_code()) {
                tooltip.add(Component.translatable("tradeautomat.recipe.code_needed"));
                tooltip.add(Component.translatable("tradeautomat.recipe.code_range", recipe.min_code(), recipe.max_code()));
            } else {
                tooltip.add(Component.translatable("tradeautomat.recipe.work_without_code"));
            }
        }
    }
}