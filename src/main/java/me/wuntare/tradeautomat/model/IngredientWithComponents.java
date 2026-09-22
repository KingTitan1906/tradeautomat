package me.wuntare.tradeautomat.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.Optional;

public record IngredientWithComponents(Ingredient item, Optional<DataComponentPatch> components) {

    public static final Codec<IngredientWithComponents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Ingredient.CODEC.fieldOf("item").forGetter(IngredientWithComponents::item),
            DataComponentPatch.CODEC.optionalFieldOf("components").forGetter(IngredientWithComponents::components)
    ).apply(instance, IngredientWithComponents::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, IngredientWithComponents> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, IngredientWithComponents::item,
            ByteBufCodecs.optional(DataComponentPatch.STREAM_CODEC), IngredientWithComponents::components,
            IngredientWithComponents::new
    );

    public boolean test(ItemStack stack) {
        if (!this.item.test(stack)) {
            return false;
        }
        if (this.components.isPresent() && !this.components.get().isEmpty()) {
            DataComponentPatch required = this.components.get();
            var playerComponents = stack.getComponents();

            for (var entry : required.entrySet()) {
                var key = entry.getKey();
                var expectedVal = entry.getValue().orElse(null);
                var actualVal = playerComponents.get(key);

                if (expectedVal == null ? actualVal != null : !expectedVal.equals(actualVal)) {
                    return false;
                }
            }
        }
        return true;
    }

    public List<ItemStack> getMatchingStacksList() {
        return this.item.items()
                .map(holder -> {
                    ItemStack stack = new ItemStack(holder);
                    this.components.ifPresent(stack::applyComponents);
                    return stack;
                })
                .toList();
    }
}