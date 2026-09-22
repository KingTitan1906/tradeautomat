package me.wuntare.tradeautomat.client.gui;

import me.wuntare.tradeautomat.client.GuiRenderUtils;
import me.wuntare.tradeautomat.gui.AutomatStorageMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class AutomatStorageScreen extends AbstractContainerScreen<AutomatStorageMenu> {
    public static final int MAIN_PANEL_WIDTH = 176;
    public static final int MAIN_PANEL_HEIGHT = 150;
    public static final int PUT_PANEL_WIDTH = 176;
    public static final int PUT_PANEL_HEIGHT = 332;
    public static final int PANEL_GAP = 4;

    public AutomatStorageScreen(AutomatStorageMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.titleLabelX = (MAIN_PANEL_WIDTH - this.font.width(title)) / 2;
        this.titleLabelY = -30;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 73;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);

        GuiRenderUtils.drawMenu(graphics, this.leftPos, this.topPos+18, MAIN_PANEL_WIDTH, MAIN_PANEL_HEIGHT);
        GuiRenderUtils.drawMenu(graphics, this.leftPos + MAIN_PANEL_WIDTH + PANEL_GAP, this.topPos-83, PUT_PANEL_WIDTH, PUT_PANEL_HEIGHT);
        GuiRenderUtils.drawMenu(graphics, this.leftPos - PANEL_GAP - PUT_PANEL_WIDTH, this.topPos-83, PUT_PANEL_WIDTH, PUT_PANEL_HEIGHT);
        GuiRenderUtils.drawMenu(graphics, this.leftPos+((MAIN_PANEL_WIDTH-120)/2), this.topPos-44, 120, 20);

        GuiRenderUtils.drawStorageMenuSlots(graphics, this.leftPos, this.topPos, this.menu);

        graphics.text(this.font, "Input", this.leftPos - PANEL_GAP - PUT_PANEL_WIDTH+8, this.topPos-77, 0xFF404040, false);
        graphics.text(this.font, "Output", this.leftPos + MAIN_PANEL_WIDTH + PANEL_GAP+8, this.topPos-77, 0xFF404040, false);
        graphics.text(this.font, "Module", this.leftPos + 8, this.topPos+23, 0xFF404040, false);
    }

    @Override
    protected boolean hasClickedOutside(double mx, double my, int xo, int yo) {
        int mainSideY = yo + 23;
        boolean insideMain = mx >= xo && mx < xo + MAIN_PANEL_WIDTH
                && my >= mainSideY && my < mainSideY + MAIN_PANEL_HEIGHT;

        int leftX = xo - PANEL_GAP - PUT_PANEL_WIDTH;
        int sideY = yo - 83;
        boolean insideLeft = mx >= leftX && mx < leftX + PUT_PANEL_WIDTH
                && my >= sideY && my < sideY + PUT_PANEL_HEIGHT;

        int rightX = xo + MAIN_PANEL_WIDTH + PANEL_GAP;
        boolean insideRight = mx >= rightX && mx < rightX + PUT_PANEL_WIDTH
                && my >= sideY && my < sideY + PUT_PANEL_HEIGHT;

        return !insideMain && !insideLeft && !insideRight;
    }
}