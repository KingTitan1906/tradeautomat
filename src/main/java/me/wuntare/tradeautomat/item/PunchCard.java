package me.wuntare.tradeautomat.item;

import me.wuntare.tradeautomat.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class PunchCard extends Item {
    public PunchCard(Properties props) {
        props.stacksTo(1);
        props.component(ModDataComponents.CODE, "00000000");
        super(props);
    }

    public String getCode(ItemStack itemStack) {
        return itemStack.getOrDefault(ModDataComponents.CODE, "00000000");
    }

    public void setCode(ItemStack itemStack, String code) {
        itemStack.set(ModDataComponents.CODE, code);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        if (itemStack.has(ModDataComponents.CODE)) {
            builder.accept(Component.translatable("item.tradeautomat.punch_card.tooltip", getCode(itemStack)).withStyle(ChatFormatting.GRAY));
        }
        super.appendHoverText(itemStack, context, display, builder, tooltipFlag);
    }
}
