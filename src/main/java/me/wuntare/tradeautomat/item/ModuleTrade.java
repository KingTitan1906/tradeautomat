package me.wuntare.tradeautomat.item;

import me.wuntare.tradeautomat.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class ModuleTrade extends Item {
    public ModuleTrade(Properties props) {
        props.stacksTo(64);
        props.component(ModDataComponents.MODULE_LEVEL, 1);
        super(props);
    }

    public int getModuleLevel(ItemStack itemStack) {
        return itemStack.getOrDefault(ModDataComponents.MODULE_LEVEL, 1);
    }

    public int updateModuleLevel(ItemStack itemStack, int level) {
        int currentLevel = getModuleLevel(itemStack);
        if (level > currentLevel) {
            itemStack.set(ModDataComponents.MODULE_LEVEL, level);
            return level;
        }
        return currentLevel;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        if (itemStack.has(ModDataComponents.MODULE_LEVEL)) {
            builder.accept(Component.translatable("item.tradeautomat.module_trade.tooltip", getModuleLevel(itemStack)).withStyle(ChatFormatting.GRAY));
        }
        super.appendHoverText(itemStack, context, display, builder, tooltipFlag);
    }
}
