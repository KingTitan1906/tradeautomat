package me.wuntare.tradeautomat.client.gui;

import me.wuntare.tradeautomat.client.GuiRenderUtils;
import me.wuntare.tradeautomat.gui.EngineeringTerminalMenu;
import me.wuntare.tradeautomat.network.SetTerminalCodePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class EngineeringTerminalScreen extends AbstractContainerScreen<EngineeringTerminalMenu> {
    private EditBox codeEditBox;

    public EngineeringTerminalScreen(EngineeringTerminalMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();

        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        int x = this.leftPos;
        int y = this.topPos;

        this.codeEditBox = new EditBox(
                this.font,
                x + 20, y + 54,
                136, 16,
                Component.translatable("tradeautomat.enter_code")
        );

        this.codeEditBox.setMaxLength(8);

        if (this.menu.getBlockEntity() != null) {
            this.codeEditBox.setValue(this.menu.getBlockEntity().getCurrentCode());
        }

        this.codeEditBox.setResponder(this::onCodeChanged);

        this.updateEditBoxState();

        this.addRenderableWidget(this.codeEditBox);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        if (this.codeEditBox != null) {
            this.updateEditBoxState();
        }
    }

    @Override
    public void onClose() {
        if (this.menu.getBlockEntity() != null) {
            this.menu.getBlockEntity().setCurrentCode("");
            ClientPlayNetworking.send(new SetTerminalCodePayload(
                    this.menu.getBlockEntity().getBlockPos(),
                    ""
            ));
        }
        super.onClose();
    }

    private void updateEditBoxState() {
        boolean requiresCode = this.menu.isCodeRequired();

        this.codeEditBox.setVisible(requiresCode);
        this.codeEditBox.setEditable(requiresCode);
    }

    private void onCodeChanged(String newCode) {
        String filtered = newCode.replaceAll("[^0-9]", "");

        if (!newCode.equals(filtered)) {
            this.codeEditBox.setValue(filtered);
            return;
        }

        this.menu.updateCodeFromClient(filtered);

        if (this.menu.getBlockEntity() != null) {
            ClientPlayNetworking.send(new SetTerminalCodePayload(
                    this.menu.getBlockEntity().getBlockPos(),
                    filtered
            ));
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        int x = this.leftPos;
        int y = this.topPos;

        GuiRenderUtils.drawMenu(graphics, x, y, this.imageWidth, this.imageHeight);
        GuiRenderUtils.drawMenuSlots(graphics, x, y, this.menu);
        GuiRenderUtils.drawArrow(graphics, x + 102, y + 33);
    }
}