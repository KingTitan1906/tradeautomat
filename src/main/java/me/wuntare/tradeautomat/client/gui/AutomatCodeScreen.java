package me.wuntare.tradeautomat.client.gui;

import me.wuntare.tradeautomat.network.SetCodePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public class AutomatCodeScreen extends Screen {

    private EditBox codeInput;
    private Button submitButton;

    private final BlockPos pos;

    public AutomatCodeScreen(BlockPos pos) {
        super(Component.translatable("tradeautomat.enter_code"));
        this.pos = pos;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.submitButton = Button.builder(Component.translatable("tradeautomat.submit"), button -> {
                    String code = this.codeInput.getValue();

                    ClientPlayNetworking.send(new SetCodePayload(this.pos, code));

                    this.onClose();
                })
                .bounds(centerX - 50, centerY + 10, 100, 20)
                .build();

        this.submitButton.active = false;

        this.codeInput = new EditBox(
                this.font,
                centerX - 50,
                centerY - 20,
                100,
                20,
                Component.translatable("tradeautomat.enter_code")
        );

        this.codeInput.setMaxLength(8);

        this.codeInput.setResponder(text -> {
            String filtered = text.replaceAll("[^0-9]", "");

            if (!text.equals(filtered)) {
                this.codeInput.setValue(filtered);
                return;
            }

            this.submitButton.active = (filtered.length() == 8);
        });

        this.addRenderableWidget(this.codeInput);
        this.addRenderableWidget(this.submitButton);
    }
}
