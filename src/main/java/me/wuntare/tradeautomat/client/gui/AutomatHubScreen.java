package me.wuntare.tradeautomat.client.gui;

import me.wuntare.tradeautomat.network.SelectMenuPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public class AutomatHubScreen extends Screen {

    private final BlockPos pos;

    public AutomatHubScreen(BlockPos pos) {
        super(Component.literal("Automat Management"));
        this.pos = pos;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int buttonWidth = 130;
        int buttonHeight = 20;

        this.addRenderableWidget(Button.builder(Component.translatable("tradeautomat.trades"), button -> {
                    ClientPlayNetworking.send(new SelectMenuPayload(this.pos, 0));
                })
                .bounds(centerX - buttonWidth / 2, centerY - 35, buttonWidth, buttonHeight)
                .build());

        this.addRenderableWidget(Button.builder(Component.translatable("tradeautomat.inventory"), button -> {
                    ClientPlayNetworking.send(new SelectMenuPayload(this.pos, 1));
                })
                .bounds(centerX - buttonWidth / 2, centerY - 5, buttonWidth, buttonHeight)
                .build());

        this.addRenderableWidget(Button.builder(Component.translatable("tradeautomat.edit_code"), button -> {
                    Minecraft.getInstance().setScreenAndShow(new AutomatCodeScreen(this.pos));
                })
                .bounds(centerX - buttonWidth / 2, centerY + 25, buttonWidth, buttonHeight)
                .build());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}