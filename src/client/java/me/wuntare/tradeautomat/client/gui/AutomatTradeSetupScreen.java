package me.wuntare.tradeautomat.client.gui;

import me.wuntare.tradeautomat.client.GuiRenderUtils;
import me.wuntare.tradeautomat.gui.AutomatTradeSetupMenu;
import me.wuntare.tradeautomat.client.gui.util.ScrollController;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class AutomatTradeSetupScreen extends AbstractContainerScreen<AutomatTradeSetupMenu> {
    private static final int MAIN_PANEL_WIDTH = 176;
    private static final int MAIN_PANEL_HEIGHT = 196;
    private final ScrollController scrollController = new ScrollController();

    public AutomatTradeSetupScreen(AutomatTradeSetupMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.inventoryLabelY = 102;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - MAIN_PANEL_WIDTH) / 2;
        this.topPos = (this.height - MAIN_PANEL_HEIGHT) / 2;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        int x = this.leftPos;
        int y = this.topPos;

        GuiRenderUtils.drawMenu(graphics, x, y, MAIN_PANEL_WIDTH, MAIN_PANEL_HEIGHT);

        GuiRenderUtils.drawMenuSlots(graphics, x, y, this.menu);

        int visibleRows = AutomatTradeSetupMenu.VISIBLE_ROWS;
        int startX = this.leftPos + 53;
        int startY = this.topPos;

        for (int i = 0; i < visibleRows; i++) {
            int arrowX = startX;
            int arrowY = startY + 19 + (i * 20);

            GuiRenderUtils.drawArrow(graphics, arrowX, arrowY);
        }
    }

    private void renderScrollbar(GuiGraphicsExtractor graphics, int x, int y, int totalTrades) {
        graphics.fill(x, y, x + 10, y + 86, 0xFF202020);

        int scrollbarHeight = 86;
        int thumbHeight = Math.max(12, (int) ((float) AutomatTradeSetupMenu.VISIBLE_ROWS / totalTrades * scrollbarHeight));
        int thumbY = y + (int) (scrollController.getScrollAmount() * (scrollbarHeight - thumbHeight));

        graphics.fill(x, thumbY, x + 10, thumbY + thumbHeight, 0xFF8B8B8B);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int totalTrades = this.menu.getBlockEntity().getUnlockedTradeOffers();
        if (totalTrades > AutomatTradeSetupMenu.VISIBLE_ROWS) {
            scrollController.onMouseScrolled(verticalAmount, totalTrades, AutomatTradeSetupMenu.VISIBLE_ROWS);
            this.menu.setScrollOffset(scrollController.getRowOffset(totalTrades, AutomatTradeSetupMenu.VISIBLE_ROWS));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }



    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        boolean button = event.button() == 0;
        int x = this.leftPos + 156;
        int y = this.topPos + 18;
        double mouseX = event.x();
        double mouseY = event.y();
        if (button && scrollController.onMouseClicked(mouseX, mouseY, x, y, 10, 86)) {
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        boolean button = event.button() == 0;
        if (button) {
            scrollController.onMouseReleased();
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        int totalTrades = this.menu.getBlockEntity().getUnlockedTradeOffers();
        if (scrollController.isDragging() && totalTrades > AutomatTradeSetupMenu.VISIBLE_ROWS) {
            scrollController.onMouseDragged(event.y(), this.topPos + 18, 86);
            this.menu.setScrollOffset(scrollController.getRowOffset(totalTrades, AutomatTradeSetupMenu.VISIBLE_ROWS));
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop) {
        boolean insideMain = mouseX >= guiLeft && mouseX < guiLeft + MAIN_PANEL_WIDTH
                && mouseY >= guiTop && mouseY < guiTop + MAIN_PANEL_HEIGHT;

        return !insideMain;
    }
}