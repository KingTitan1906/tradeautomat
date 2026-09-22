package me.wuntare.tradeautomat.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.wuntare.tradeautomat.model.IngredientWithComponents;
import me.wuntare.tradeautomat.registry.ModDataComponents;
import me.wuntare.tradeautomat.registry.ModRecipes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record EngineeringTerminalRecipe(
        IngredientWithComponents base,
        IngredientWithComponents mat1,
        Optional<IngredientWithComponents> mat2,
        int min_code,
        int max_code,
        boolean requires_code,
        ItemStackTemplate result
) implements Recipe<EngineeringTerminalRecipeInput> {

    public static final MapCodec<EngineeringTerminalRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            IngredientWithComponents.CODEC.fieldOf("base").forGetter(EngineeringTerminalRecipe::base),
            IngredientWithComponents.CODEC.fieldOf("material_first").forGetter(EngineeringTerminalRecipe::mat1),
            IngredientWithComponents.CODEC.optionalFieldOf("material_second").forGetter(EngineeringTerminalRecipe::mat2),
            Codec.INT.optionalFieldOf("min_code", 0).forGetter(EngineeringTerminalRecipe::min_code),
            Codec.INT.optionalFieldOf("max_code", 99999999).forGetter(EngineeringTerminalRecipe::max_code),
            Codec.BOOL.optionalFieldOf("requires_code", false).forGetter(EngineeringTerminalRecipe::requires_code),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(EngineeringTerminalRecipe::result)
    ).apply(instance, EngineeringTerminalRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EngineeringTerminalRecipe> STREAM_CODEC = StreamCodec.composite(
            IngredientWithComponents.STREAM_CODEC, EngineeringTerminalRecipe::base,
            IngredientWithComponents.STREAM_CODEC, EngineeringTerminalRecipe::mat1,
            ByteBufCodecs.optional(IngredientWithComponents.STREAM_CODEC), EngineeringTerminalRecipe::mat2,
            ByteBufCodecs.INT, EngineeringTerminalRecipe::min_code,
            ByteBufCodecs.INT, EngineeringTerminalRecipe::max_code,
            ByteBufCodecs.BOOL, EngineeringTerminalRecipe::requires_code,
            ItemStackTemplate.STREAM_CODEC, EngineeringTerminalRecipe::result,
            EngineeringTerminalRecipe::new
    );

    public boolean matchesCode(String inputCode) {
        if (!requires_code) return true;

        if (inputCode == null || inputCode.isEmpty() || inputCode.length() != 8) {
            return false;
        }

        try {
            int codeInt = Integer.parseInt(inputCode);
            return codeInt >= min_code && codeInt <= max_code;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public boolean matches(EngineeringTerminalRecipeInput input, Level level) {
        if (!this.base.test(input.base())) {
            return false;
        }

        if (!this.mat1.test(input.mat1())) {
            return false;
        }

        if (this.mat2.isPresent()) {
            if (!this.mat2.get().test(input.mat2())) {
                return false;
            }
        } else {
            if (!input.mat2().isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack assemble(EngineeringTerminalRecipeInput input) {
        ItemStack output = result.create().copy();
        ItemStack baseItem = input.base();

        if (requires_code && input.code() != null && !input.code().isEmpty()) {
            output.set(ModDataComponents.CODE, input.code());
        }
        else if (!baseItem.isEmpty() && baseItem.has(ModDataComponents.CODE)) {
            output.set(ModDataComponents.CODE, baseItem.get(ModDataComponents.CODE));
        }

        if (!baseItem.isEmpty() && baseItem.has(net.minecraft.core.component.DataComponents.CUSTOM_NAME)) {
            output.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, baseItem.get(net.minecraft.core.component.DataComponents.CUSTOM_NAME));
        }

        return output;
    }

    @Override
    public boolean showNotification() {
        return true;
    }

    @Override
    public String group() {
        return "Engineering";
    }

    @Override
    public RecipeSerializer<? extends Recipe<EngineeringTerminalRecipeInput>> getSerializer() {
        return ModRecipes.ENGINEERING_TERMINAL_SERIALIZER;
    }

    @Override
    public RecipeType<? extends Recipe<EngineeringTerminalRecipeInput>> getType() {
        return ModRecipes.ENGINEERING_TERMINAL_TYPE;
    }

    @Override
    public PlacementInfo placementInfo() {
        List<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(this.base.item());
        ingredients.add(this.mat1.item());
        this.mat2.ifPresent((a) -> ingredients.add(this.mat2.get().item()));
        return PlacementInfo.create(ingredients);
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }
}