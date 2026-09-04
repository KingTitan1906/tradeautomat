package me.wuntare.tradeautomat.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TradeOffer {

    public static final int MAX_INPUTS = 9;
    public static final int MAX_OUTPUTS = 6;

    public static final Codec<TradeOffer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.OPTIONAL_CODEC.listOf().fieldOf("inputs").forGetter(TradeOffer::getInputs),
            ItemStack.OPTIONAL_CODEC.listOf().fieldOf("outputs").forGetter(TradeOffer::getOutputs),
            Codec.BOOL.fieldOf("active").forGetter(TradeOffer::isActive)
    ).apply(instance, TradeOffer::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TradeOffer> STREAM_CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list()), TradeOffer::getInputs,
            ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list()), TradeOffer::getOutputs,
            ByteBufCodecs.BOOL, TradeOffer::isActive,
            TradeOffer::new
    );

    private final List<ItemStack> inputs;
    private final List<ItemStack> outputs;
    private boolean active;

    public TradeOffer() {
        this(new ArrayList<>(), new ArrayList<>(), true);
    }

    public TradeOffer(List<ItemStack> inputs, List<ItemStack> outputs, boolean active) {
        this.inputs = new ArrayList<>(inputs.stream().map(ItemStack::copy).toList());
        this.outputs = new ArrayList<>(outputs.stream().map(ItemStack::copy).toList());
        this.active = active;
    }

    public ItemStack getInput(int index) {
        return (index >= 0 && index < inputs.size()) ? inputs.get(index) : ItemStack.EMPTY;
    }

    public ItemStack getOutput(int index) {
        return (index >= 0 && index < outputs.size()) ? outputs.get(index) : ItemStack.EMPTY;
    }

    public void setInput(int index, ItemStack stack) {
        if (index < 0 || index >= MAX_INPUTS) return;
        while (inputs.size() <= index) inputs.add(ItemStack.EMPTY);
        inputs.set(index, stack.copy());
    }

    public void setOutput(int index, ItemStack stack) {
        if (index < 0 || index >= MAX_OUTPUTS) return;
        while (outputs.size() <= index) outputs.add(ItemStack.EMPTY);
        outputs.set(index, stack.copy());
    }

    public boolean isValid() {
        boolean hasInput = inputs.stream().anyMatch(s -> !s.isEmpty());
        boolean hasOutput = outputs.stream().anyMatch(s -> !s.isEmpty());
        return hasInput && hasOutput;
    }

    public List<ItemStack> getInputs() { return inputs; }
    public List<ItemStack> getOutputs() { return outputs; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public TradeOffer copy() {
        return new TradeOffer(this.inputs, this.outputs, this.active);
    }
}